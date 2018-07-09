package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.DecisionStateService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class DecisionControllerTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private DecisionService decisionService;

    @Mock
    private DecisionStateService decisionStateService;

    @InjectMocks
    private DecisionController decisionController;

    @Autowired
    private MockMvc mockMvc;

    private String endpoint;

    private DecisionRequest request;

    private DecisionResponse response;

    private Answer answer;

    private UUID uuid;

    private OnlineHearing onlineHearing;

    private Decision decision;

    private DecisionState decisionState;

    private UUID decisionUUID;

    private Date expiryDate;

    @Before
    public void setup() throws IOException {
        decisionState = new DecisionState();
        decisionState.setState("decision_drafted");
        uuid = UUID.randomUUID();
        decisionUUID = UUID.randomUUID();
        expiryDate = new Date();

        endpoint = "/continuous-online-hearings/" + uuid + "/decisions";

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(uuid);

        request = (DecisionRequest) JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);
        response = (DecisionResponse) JsonUtils.toObjectFromTestName("decision/standard_decision_response", DecisionResponse.class);

        decision = new Decision();
        decision.setDecisionId(decisionUUID);
        decision.setOnlineHearing(onlineHearing);
        decision.setDecisionHeader(response.getDecisionHeader());
        decision.setDecisionText(response.getDecisionText());
        decision.setDecisionReason(response.getDecisionReason());
        decision.setDecisionAward(response.getDecisionAward());
        decision.setDeadlineExpiryDate(expiryDate);
        decision.setDecisionstate(decisionState);

        mockMvc = MockMvcBuilders.standaloneSetup(decisionController).build();
        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.of(onlineHearing));
        given(decisionService.createDecision(any(Decision.class))).willReturn(decision);
        given(decisionStateService.retrieveDecisionStateByState("decision_drafted")).willReturn(Optional.ofNullable(decisionState));
        given(decisionService.retrieveByOnlineHearingIdAndDecisionId(uuid, decisionUUID)).willReturn(Optional.of(decision));
    }

    @Test
    public void testCreateDecision() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testCreateDuplicateDecision() throws Exception {

        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));

        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateDecisionForNonExistentOnlineHearing() throws Exception {

        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.empty());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Online hearing not found", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateDecisionForNonExistentDecisionState() throws Exception {

        given(decisionStateService.retrieveDecisionStateByState("decision_drafted")).willReturn(Optional.empty());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        assertEquals("Unable to retrieve starting state for decision", result.getResponse().getContentAsString());
    }

    @Test
    public void testEmptyDecisionHeader() throws Exception {

        request.setDecisionHeader(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision header is required");
    }

    @Test
    public void testEmptyDecisionText() throws Exception {

        request.setDecisionText(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision text is required");
    }

    @Test
    public void testEmptyDecisionReason() throws Exception {

        request.setDecisionReason(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision reason is required");
    }

    @Test
    public void testEmptyDecisionAward() throws Exception {

        request.setDecisionAward(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision award is required");
    }

    @Test
    public void testGetDecision() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/" + decisionUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        DecisionResponse expected = (DecisionResponse) JsonUtils.toObjectFromTestName("decision/standard_decision_response", DecisionResponse.class);
        expected.setDecisionId(decisionUUID.toString());
        expected.setOnlineHearingId(uuid.toString());
        expected.setDeadlineExpiryDate(expiryDate.toString());
        DecisionResponse actual = (DecisionResponse) JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), DecisionResponse.class);

        assertEquals(expected.getDecisionId(), actual.getDecisionId());
        assertEquals(expected.getOnlineHearingId(), actual.getOnlineHearingId());
        assertEquals(expected.getDecisionHeader(), actual.getDecisionHeader());
        assertEquals(expected.getDecisionText(), actual.getDecisionText());
        assertEquals(expected.getDecisionReason(), actual.getDecisionReason());
        assertEquals(expected.getDecisionAward(), actual.getDecisionAward());
        assertEquals(expected.getDeadlineExpiryDate(), actual.getDeadlineExpiryDate());
        assertEquals(expected.getDecisionState().getStateName(), actual.getDecisionState().getStateName());
    }

    @Test
    public void testGetDecisionNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound());
    }
}