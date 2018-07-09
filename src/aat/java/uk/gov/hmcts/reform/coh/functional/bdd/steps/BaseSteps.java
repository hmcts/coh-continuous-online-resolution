package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.CreateOnlineHearingResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseSteps {
    private static final Logger log = LoggerFactory.getLogger(BaseSteps.class);

    protected RestTemplate restTemplate;

    private Map<String, String> endpoints = new HashMap<String, String>();

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Value("${base-urls.test-url}")
    String baseUrl;

    protected TestContext testContext;

    @Autowired
    public BaseSteps(TestContext testContext) {
        this.testContext = testContext;
    }

    public void setup() throws Exception {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());

        endpoints.put("online hearing", "/continuous-online-hearings");
        endpoints.put("question", "/continuous-online-hearings/onlineHearing_id/questions");
        endpoints.put("answer", "/continuous-online-hearings/onlineHearing_id/questions/question_id/answers");
    }

    public void cleanup() {
        if (testContext.getScenarioContext().getCaseIds() != null) {
            for (String caseId : testContext.getScenarioContext().getCaseIds()) {
                try {
                    OnlineHearing onlineHearing = new OnlineHearing();
                    onlineHearing.setCaseId(caseId);
                    onlineHearing = onlineHearingService.retrieveOnlineHearingByCaseId(onlineHearing);
                    onlineHearingPanelMemberRepository.deleteByOnlineHearing(onlineHearing);
                    onlineHearingService.deleteByCaseId(caseId);
                } catch (DataIntegrityViolationException e) {
                    log.error("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
                }
            }
        }
    }

    OnlineHearing createOnlineHearingFromResponse(CreateOnlineHearingResponse response) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString(response.getOnlineHearingId()));

        return onlineHearing;
    }

    OnlineHearing createOnlineHearingFromResponse(OnlineHearingResponse response) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(response.getOnlineHearingId());

        return onlineHearing;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }
}
