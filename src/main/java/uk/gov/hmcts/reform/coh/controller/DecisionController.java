package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.coh.controller.decision.*;
import uk.gov.hmcts.reform.coh.controller.validators.DecisionRequestValidator;
import uk.gov.hmcts.reform.coh.controller.validators.Validation;
import uk.gov.hmcts.reform.coh.controller.validators.ValidationResult;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.DecisionStateService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/continuous-online-hearings/{onlineHearingId}/decisions")
public class DecisionController {

    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    private static final String STARTING_STATE = "decision_drafted";

    private OnlineHearingService onlineHearingService;

    private DecisionService decisionService;

    private DecisionStateService decisionStateService;

    private Validation validation = new Validation();

    @Autowired
    public DecisionController(OnlineHearingService onlineHearingService, DecisionService decisionService, DecisionStateService decisionStateService) {
        this.onlineHearingService = onlineHearingService;
        this.decisionService = decisionService;
        this.decisionStateService = decisionStateService;
    }

    @ApiOperation(value = "Create decision", notes = "A POST request is used to create a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Success", response = CreateDecisionResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Online hearing not found"),
            @ApiResponse(code = 409, message = "Online hearing already contains a decision"),
            @ApiResponse(code = 422, message = "Validation error")
    })
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDecision(UriComponentsBuilder uriBuilder, @PathVariable UUID onlineHearingId, @RequestBody DecisionRequest request) {

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Online hearing already contains a decision");
        }

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearingId);
        if (!optionalOnlineHearing.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Online hearing not found");
        }

        ValidationResult result = validation.execute(DecisionRequestValidator.values(), request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        Optional<DecisionState> optionalDecisionState = decisionStateService.retrieveDecisionStateByState(STARTING_STATE);
        if (!optionalDecisionState.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Unable to retrieve starting state for decision");
        }

        Decision decision = new Decision();
        decision.setOnlineHearing(optionalOnlineHearing.get());
        DecisionRequestMapper.map(request, decision, optionalDecisionState.get());
        decision.addDecisionStateHistory(optionalDecisionState.get());
        decision = decisionService.createDecision(decision);
        CreateDecisionResponse response = new CreateDecisionResponse();
        response.setDecisionId(decision.getDecisionId());

        UriComponents uriComponents =
                uriBuilder.path("/continuous-online-hearings/{onlineHearingId}/decisions").buildAndExpand(onlineHearingId);

        return ResponseEntity.created(uriComponents.toUri()).body(response);
    }

    @ApiOperation(value = "Get decision", notes = "A GET request to retrieve a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = DecisionResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @GetMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity retrieveDecision(@PathVariable UUID onlineHearingId) {

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to find decision");
        }

        DecisionResponse decisionResponse = new DecisionResponse();
        DecisionResponseMapper.map(optionalDecision.get(), decisionResponse);
        return new ResponseEntity<>(decisionResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Update a decision", notes = "A PUT request to update replace a decision")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Conflict"),
            @ApiResponse(code = 422, message = "Validation Error")
    })

    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDecision(@PathVariable UUID onlineHearingId, @RequestBody UpdateDecisionRequest request) {

        Optional<Decision> optionalDecision = decisionService.findByOnlineHearingId(onlineHearingId);
        if (!optionalDecision.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Decision not found");
        }

        // Only draft decisions can be updated
        Decision decision = optionalDecision.get();
        if (!decision.getDecisionstate().getState().equals(DecisionsStates.DECISION_DRAFTED.getStateName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Only draft decisions can be updated");
        }

        // Check the stated passed in the request
        Optional<DecisionState> optionalDecisionState = decisionStateService.retrieveDecisionStateByState(request.getState());
        if (!optionalDecisionState.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid state");
        }

        // The remaining validation is same as DecisionRequest for create
        ValidationResult result = validation.execute(DecisionRequestValidator.values(), request);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result.getReason());
        }

        // If a decision is issued, then there is a deadline to accept or reject it
        if (request.getState().equals(DecisionsStates.DECISION_ISSUED.getStateName())) {
            decision.setDeadlineExpiryDate(ExpiryCalendar.getDeadlineExpiryDate());
        }

        decision.addDecisionStateHistory(optionalDecisionState.get());
        DecisionRequestMapper.map(request, decision, optionalDecisionState.get());
        decisionService.updateDecision(decision);

        return ResponseEntity.ok("");
    }
}