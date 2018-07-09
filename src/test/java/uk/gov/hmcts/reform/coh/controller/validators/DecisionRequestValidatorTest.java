package uk.gov.hmcts.reform.coh.controller.validators;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DecisionRequestValidatorTest {

    private DecisionRequest request;

    @Before
    public void setup() throws IOException {
        request = (DecisionRequest)JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);
    }

    @Test
    public void testValidDecisionRequest() {
        ValidationResult result = DecisionRequestValidator.validate(request);
        assertTrue(result.isValid());
    }

    @Test
    public void testEmptyDecisionHeader() {
        request.setDecisionHeader(null);
        ValidationResult result = DecisionRequestValidator.validate(request);
        assertFalse(result.isValid());
        assertEquals("Decision header is required", result.getReason());
    }

    @Test
    public void testEmptyDecisionText() {
        request.setDecisionText(null);
        ValidationResult result = DecisionRequestValidator.validate(request);
        assertFalse(result.isValid());
        assertEquals("Decision text is required", result.getReason());
    }

    @Test
    public void testEmptyDecisionReason() {
        request.setDecisionReason(null);
        ValidationResult result = DecisionRequestValidator.validate(request);
        assertFalse(result.isValid());
        assertEquals("Decision reason is required", result.getReason());
    }

    @Test
    public void testEmptyDecisionAward() {
        request.setDecisionAward(null);
        ValidationResult result = DecisionRequestValidator.validate(request);
        assertFalse(result.isValid());
        assertEquals("Decision award is required", result.getReason());
    }
}
