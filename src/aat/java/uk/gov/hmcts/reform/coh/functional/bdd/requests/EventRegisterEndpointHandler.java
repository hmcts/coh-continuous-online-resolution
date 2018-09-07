package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import static org.springframework.http.HttpMethod.*;

@Component
public class EventRegisterEndpointHandler extends AbstractRequestEndpoint {

    @Override
    public String getUrl(HttpMethod method, TestContext testContext) {

        String url;
        if (POST.equals(method) || PUT.equals(method) || DELETE.equals(method)) {
            url = CohUriBuilder.buildEventRegisterPost();
        } else {
            throw new NotImplementedException(String.format("Http method %s not implemented for %s", method, getClass()));
        }

        return baseUrl + url;
    }

    @Override
    public String supports() {
        return CohEntityTypes.EVENT.toString();
    }
}
