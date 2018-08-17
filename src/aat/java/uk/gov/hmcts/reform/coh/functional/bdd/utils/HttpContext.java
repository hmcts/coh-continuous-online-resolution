package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicResponseHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

@Component
public class HttpContext {

    private String idamAuthorRef;
    private String idamServiceRef;

    private String rawResponseString;
    private int httpResponseStatusCode;
    private ResponseEntity responseEntity;

    public ResponseEntity getResponseEntity() {
        return responseEntity;
    }

    public void setResponseBodyAndStatesForResponse(HttpResponse httpResponse) throws IOException {
        rawResponseString = new BasicResponseHandler().handleResponse(httpResponse);
        httpResponseStatusCode = httpResponse.getStatusLine().getStatusCode();
    }

    public void setResponseBodyAndStatesForResponse(ResponseEntity<String> responseEntity) throws IOException {
        this.responseEntity = responseEntity;
        rawResponseString = responseEntity.getBody();
        httpResponseStatusCode = responseEntity.getStatusCodeValue();
    }

    public void setResponseBodyAndStatesForException(HttpClientErrorException hcee) {
        rawResponseString = hcee.getResponseBodyAsString();
        httpResponseStatusCode = hcee.getRawStatusCode();
    }

    public String getRawResponseString() {
        return rawResponseString;
    }

    public void setRawResponseString(String rawResponseString) {
        this.rawResponseString = rawResponseString;
    }

    public int getHttpResponseStatusCode() {
        return httpResponseStatusCode;
    }

    public void setHttpResponseStatusCode(int httpResponseStatusCode) {
        this.httpResponseStatusCode = httpResponseStatusCode;
    }

    public String getIdamAuthorRef() {
        return idamAuthorRef;
    }

    public void setIdamAuthorRef(String idamAuthorRef) {
        this.idamAuthorRef = idamAuthorRef;
    }

    public String getIdamServiceRef() {
        return idamServiceRef;
    }

    public void setIdamServiceRef(String idamServiceRef) {
        this.idamServiceRef = idamServiceRef;
    }
}
