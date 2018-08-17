package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.*;
import uk.gov.hmcts.reform.coh.schedule.notifiers.EventNotifierJob;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
public class ApiSteps extends BaseSteps {

    private static final Logger log = LoggerFactory.getLogger(ApiSteps.class);

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Autowired
    private SessionEventTypeRespository sessionEventTypeRespository;

    @Autowired
    private SessionEventService sessionEventService;

    @Autowired
    private EventNotifierJob eventNotifierJob;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    private JSONObject json;

    private CloseableHttpClient httpClient;

    private RestTemplate restTemplate;

    private Set<String> caseIds;
    private List<Jurisdiction> jurisdictions;

    @Autowired
    public ApiSteps(TestContext testContext) {
        super(testContext);
    }

    @Before
    public void setUp() throws Exception {
        super.setup();
        caseIds = new HashSet<>();
        httpClient = HttpClientBuilder
            .create()
            .setSslcontext(new SSLContextBuilder()
                .loadTrustMaterial(null, TestTrustManager.getInstance().getTrustStrategy())
                .build())
            .build();
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());
        jurisdictions = new ArrayList<>();
        testContext.getScenarioContext().setJurisdictions(jurisdictions);
    }

    @After
    public void cleanUp() {
        for (String caseId : caseIds) {
            try {
                OnlineHearing onlineHearing = new OnlineHearing();
                onlineHearing.setCaseId(caseId);
                onlineHearing = onlineHearingService.retrieveOnlineHearingByCaseId(onlineHearing);
                onlineHearingPanelMemberRepository.deleteByOnlineHearing(onlineHearing);
                onlineHearingService.deleteByCaseId(caseId);
            } catch (DataIntegrityViolationException e) {
                log.error(
                    "Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
            }
        }
        if (testContext.getScenarioContext().getSessionEventForwardingRegisters() != null) {
            for (SessionEventForwardingRegister sessionEventForwardingRegister : testContext.getScenarioContext()
                .getSessionEventForwardingRegisters()) {
                try {
                    sessionEventForwardingRegisterRepository.delete(sessionEventForwardingRegister);
                } catch (DataIntegrityViolationException e) {
                    log.error(
                        "Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
                }
            }
        }
        for (Jurisdiction jurisdiction : testContext.getScenarioContext().getJurisdictions()) {
            try {
                jurisdictionRepository.delete(jurisdiction);
            } catch (DataIntegrityViolationException e) {
                log.error(
                    "Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
            }
        }
    }

    @When("^a get request is sent to ' \"([^\"]*)\"' for the saved online hearing$")
    public void a_get_request_is_sent_to(String endpoint) throws Throwable {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        String url = baseUrl + endpoint + "/" + onlineHearing.getOnlineHearingId().toString();
        HttpEntity<String> request = new HttpEntity<>("", header);
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
    }

    @When("^a get request is sent to ' \"([^\"]*)\"' for the online hearing$")
    public void a_filter_get_request_is_sent_to(String endpoint) throws Throwable {
        RestTemplate restTemplate = getRestTemplate();
        HttpEntity<String> request = new HttpEntity<>("", header);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
    }

    @When("^a post request is sent to ' \"([^\"]*)\"'$")
    public void a_post_request_is_sent_to(String endpoint) throws Throwable {
        RestTemplate restTemplate = getRestTemplate();
        HttpEntity<String> request = new HttpEntity<>(json.toString(), header);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
    }

    @Then("^the response code is (\\d+)$")
    public void the_response_code_is(int responseCode) throws Throwable {
        assertEquals("Response status code", responseCode, testContext.getHttpContext().getHttpResponseStatusCode());
    }

    @Then("^the response contains the following text '\"([^\"]*)\" '$")
    public void the_response_contains_the_following_text(String text) throws IOException {
        assertTrue(testContext.getHttpContext().getRawResponseString().contains(text));
    }

    @Then("^the response contains the online hearing UUID$")
    public void the_response_contains_the_online_hearing_UUID() throws IOException {
        String responseString = testContext.getHttpContext().getRawResponseString();
        CreateOnlineHearingResponse response = JsonUtils
            .toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
        assertEquals(response.getOnlineHearingId(), UUID.fromString(response.getOnlineHearingId()).toString());
    }

    @Given("^a standard online hearing is created$")
    public void aStandardOnlineHearingIsCreated() throws Throwable {
        String jsonBody = JsonUtils.getJsonInput("online_hearing/standard_online_hearing");

        OnlineHearingRequest onlineHearingRequest = JsonUtils.toObjectFromJson(jsonBody, OnlineHearingRequest.class);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        try {
            ResponseEntity<String> response = restTemplate
                .exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
            String responseString = response.getBody();
            testContext.getScenarioContext().setCurrentOnlineHearing(onlineHearingRequest);
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);

            CreateOnlineHearingResponse newOnlineHearing = JsonUtils
                .toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
            testContext.getScenarioContext().getCurrentOnlineHearing()
                .setOnlineHearingId(UUID.fromString(newOnlineHearing.getOnlineHearingId()));
            testContext.getScenarioContext().addCaseId(onlineHearingRequest.getCaseId());

            testContext.getScenarioContext()
                .setCurrentOnlineHearing(onlineHearingRepository.findByCaseId(onlineHearingRequest.getCaseId()).get());
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForException(hcee);
        }
    }

    @And("^the online hearing jurisdiction is ' \"([^\"]*)\" '$")
    public void theOnlineHearingJurisdictionIsSCSS(String jurisdictionName) {
        testContext.getScenarioContext().getCurrentOnlineHearingRequest().setJurisdiction(jurisdictionName);
    }

    @And("^the post request is sent to create the online hearing$")
    public void thePostRequestIsSentToCreateTheOnlineHearing() throws IOException {
        String jsonBody = JsonUtils.toJson(testContext.getScenarioContext().getCurrentOnlineHearingRequest());
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        ResponseEntity<String> response = restTemplate
            .exchange(baseUrl + "/continuous-online-hearings", HttpMethod.POST, request, String.class);
        String responseString = response.getBody();
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        CreateOnlineHearingResponse newOnlineHearing = JsonUtils
            .toObjectFromJson(responseString, CreateOnlineHearingResponse.class);
        testContext.getScenarioContext().setCurrentOnlineHearing(new OnlineHearing());
        testContext.getScenarioContext().getCurrentOnlineHearing()
            .setOnlineHearingId(UUID.fromString(newOnlineHearing.getOnlineHearingId()));
        testContext.getScenarioContext().setCurrentOnlineHearing(onlineHearingRepository
            .findByCaseId(testContext.getScenarioContext().getCurrentOnlineHearingRequest().getCaseId()).get());

    }

    @And("^a jurisdiction named ' \"([^\"]*)\", with id ' \"(\\d+)\" ' and max question rounds ' \"(\\d+)\" ' is created$")
    public void aJurisdictionNamedWithUrlAndMaxQuestionRoundsIsCreated(String jurisdictionName, Long id,
        int maxQuestionRounds) {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionId(id);
        jurisdiction.setJurisdictionName(jurisdictionName);
        jurisdiction.setMaxQuestionRounds(maxQuestionRounds);
        jurisdictionRepository.save(jurisdiction);
        jurisdictions.add(jurisdiction);
    }


    @And("^the jurisdiction is registered to receive ([^\"]*) events$")
    public void theJurisdictionIsRegisteredToReceiveQuestionRoundIssuedEvents(String eventType) {

        SessionEventType sessionEventType = sessionEventTypeRespository.findByEventTypeName(eventType)
            .orElseThrow(() -> new EntityNotFoundException());
        Jurisdiction testJurisdiction = jurisdictionRepository.findByJurisdictionName("SSCS")
            .orElseThrow(() -> new EntityNotFoundException());
        SessionEventForwardingRegister templateEFR = sessionEventForwardingRegisterRepository
            .findByJurisdictionAndSessionEventType(testJurisdiction, sessionEventType)
            .orElseThrow(() -> new EntityNotFoundException());

        SessionEventForwardingRegister sessionEventForwardingRegister = new SessionEventForwardingRegister.Builder()
            .jurisdiction(jurisdictions.get(0))
            .sessionEventType(sessionEventType)
            .forwardingEndpoint(templateEFR.getForwardingEndpoint())
            .maximumRetries(templateEFR.getMaximumRetries())
            .registrationDate(new Date())
            .withActive(true)
            .build();

        SessionEventForwardingRegister savedEFR = sessionEventForwardingRegisterRepository
            .save(sessionEventForwardingRegister);
        testContext.getScenarioContext().addSessionEventForwardingRegister(savedEFR);
    }

    @And("^the response contains (\\d+) panel member$")
    public void theResponseContainsPanelMember(int count) throws IOException {
        String rawResponseString = testContext.getHttpContext().getRawResponseString();
        ObjectMapper objMap = new ObjectMapper();
        Map<String, Object> map = objMap.readValue(rawResponseString, new TypeReference<Map<String, Object>>() {
        });

        List<String> map1 = (List<String>) map.get("panel");
        assertEquals(count, map1.size());
    }

    @And("^the response headers contains a location to the created entity$")
    public void theHeaderContainsLocationOfCreatedQuestion() {
        ResponseEntity responseEntity = testContext.getHttpContext().getResponseEntity();
        HttpHeaders headers = responseEntity.getHeaders();
        assertFalse(headers.get("Location").isEmpty());
    }

    @And("^send get request to the location$")
    public void sendGetRequestToTheLocation() {
        ResponseEntity responseEntity = testContext.getHttpContext().getResponseEntity();
        HttpHeaders headers = responseEntity.getHeaders();
        String urlToLocation = headers.get("Location").get(0);

        HttpEntity<String> request = new HttpEntity<>("", header);
        ResponseEntity<String> response = restTemplate.exchange(urlToLocation, HttpMethod.GET, request, String.class);
        testContext.getHttpContext().setHttpResponseStatusCode(response.getStatusCodeValue());
        testContext.getHttpContext().setRawResponseString(response.getBody());
    }

    @When("^an event has been queued for this online hearing of event type (.*)$")
    public void anEventHasBeenQueuedForThisOnlineHearingOfEventType(String eventType) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);

        assertFalse(sessionEvents.isEmpty());
        boolean hasEvent = sessionEvents.stream()
            .anyMatch(se -> se.getSessionEventForwardingRegister().getSessionEventType().getEventTypeName()
                .equalsIgnoreCase(eventType));
        assertTrue(hasEvent);
    }

    @And("^there is no event queued for this online hearing of event type (.*)")
    public void thereIsNoEventQueuedForThisOnlineHearingOfEventTypeAnswersSubmitted(String eventType) throws Throwable {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);

        boolean hasEvent = sessionEvents.stream()
            .noneMatch(se -> se.getSessionEventForwardingRegister().getSessionEventType().getEventTypeName()
                .equalsIgnoreCase(eventType));
        assertTrue(hasEvent);
    }

    @And("^the event has been set to forwarding_state_pending of event type (.*)$")
    public void thePutRequestIsSentToResetTheEventsOfTypeAnswerSubmitted(String eventType) {
        SessionEventType expectedEventType = sessionEventTypeRespository.findByEventTypeName(eventType)
            .orElseThrow(() -> new EntityNotFoundException());

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Jurisdiction jurisdiction = jurisdictionRepository
            .findByJurisdictionName(onlineHearing.getJurisdiction().getJurisdictionName())
            .orElseThrow(() -> new EntityNotFoundException());

        SessionEventForwardingRegisterId sessionEventForwardingRegisterId = new SessionEventForwardingRegisterId(
            jurisdiction.getJurisdictionId(), expectedEventType.getEventTypeId());

        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);
        boolean hasEvent = sessionEvents.stream()
            .filter(se -> se.getSessionEventForwardingRegister().getEventForwardingRegisterId()
                .equals(sessionEventForwardingRegisterId))
            .allMatch(se -> se.getSessionEventForwardingState().getForwardingStateName()
                .equalsIgnoreCase(SessionEventForwardingStates.EVENT_FORWARDING_PENDING.getStateName()));

        assertTrue(hasEvent);
    }

    @And("^the event type (.*) has been set to retries of (\\d+)$")
    public void theEventHasBeenSetToRetriesOf(String eventType, int expectedRetries) {
        SessionEventType expectedEventType = sessionEventTypeRespository.findByEventTypeName(eventType)
            .orElseThrow(() -> new EntityNotFoundException());

        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Jurisdiction jurisdiction = jurisdictionRepository
            .findByJurisdictionName(onlineHearing.getJurisdiction().getJurisdictionName())
            .orElseThrow(() -> new EntityNotFoundException());

        SessionEventForwardingRegisterId sessionEventForwardingRegisterId = new SessionEventForwardingRegisterId(
            jurisdiction.getJurisdictionId(), expectedEventType.getEventTypeId());

        List<SessionEvent> sessionEvents = sessionEventService.retrieveByOnlineHearing(onlineHearing);
        boolean hasExpectedRetries = sessionEvents.stream()
            .filter(se -> se.getSessionEventForwardingRegister().getEventForwardingRegisterId()
                .equals(sessionEventForwardingRegisterId))
            .allMatch(se -> se.getRetries() == expectedRetries);

        assertTrue(hasExpectedRetries);
    }
}