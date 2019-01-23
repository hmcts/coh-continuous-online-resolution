package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointFactory;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEndpointHandler;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor;
import uk.gov.hmcts.reform.coh.idam.IdamAuthentication;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingRegisterRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class BaseSteps {

    protected RestTemplate restTemplate;

    protected static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private SessionEventForwardingRegisterRepository sessionEventForwardingRegisterRepository;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${base-urls.test-url}")
    protected String baseUrl;

    @Value("${aat.test-notification-endpoint}")
    protected String testNotificationUrl;

    protected TestContext testContext;
    private final IdamAuthentication idamAuthentication;

    protected HttpHeaders header;

    @Autowired
    public BaseSteps(TestContext testContext, IdamAuthentication idamAuthentication) {
        this.testContext = testContext;
        this.idamAuthentication = idamAuthentication;
    }

    public void setup() throws Exception {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());

        prepareAuthenticationTokens();

        header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        Optional.ofNullable(testContext.getHttpContext().getIdamAuthorRef())
            .ifPresent(token -> header.add(IdamHeaderInterceptor.IDAM_AUTHORIZATION, "Bearer " + token));

        Optional.ofNullable(testContext.getHttpContext().getIdamServiceRef())
            .ifPresent(token -> header.add(IdamHeaderInterceptor.IDAM_SERVICE_AUTHORIZATION, token));
    }

    private void prepareAuthenticationTokens() {
        getIdamToken();
        getS2sToken();
    }

    private void getIdamToken() {
        testContext.getHttpContext().setIdamAuthorRef(idamAuthentication.getToken());
    }

    private void getS2sToken() {
        testContext.getHttpContext().setIdamServiceRef(authTokenGenerator.generate());
    }

    protected ResponseEntity<String> sendRequest(CohEntityTypes entity, String methodType, String payload) {
        return sendRequest(entity.toString(), methodType, payload);
    }

    protected ResponseEntity<String> sendRequest(String entity, String methodType, String payload) {
        HttpMethod method = HttpMethod.valueOf(methodType);

        CohEndpointHandler endpoint = CohEndpointFactory.getRequestEndpoint(entity);
        return sendRequest(endpoint.getUrl(method, testContext), method, payload);
    }

    protected ResponseEntity<String> sendRequest(String url, HttpMethod method, String payload) {
        HttpEntity<String> request = new HttpEntity<>(payload, header);
        return restTemplate.exchange(url, method, request, String.class);
    }

    protected ResponseEntity<String> sendRequest(String url, HttpMethod method) {
        return sendRequest(url, method, null);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
