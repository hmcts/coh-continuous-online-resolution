package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.hmcts.reform.coh.domain.RelistingState;

import javax.validation.constraints.NotNull;

public class RelistingRequest {
    private final String reason;

    @ApiModelProperty(required = true, allowableValues = "drafted, issued")
    @NotNull(message = "Missing state field")
    private final RelistingState state;

    public RelistingRequest(
        @JsonProperty("reason") String reason,
        @JsonProperty("state") RelistingState state
    ) {
        this.reason = reason;
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public RelistingState getState() {
        return state;
    }
}
