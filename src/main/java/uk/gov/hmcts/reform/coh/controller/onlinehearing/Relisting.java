package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.domain.RelistingState;

import javax.validation.constraints.NotNull;

public class Relisting {
    public final String reason;

    @NotNull(message = "Missing state field")
    public final RelistingState state;

    public Relisting(
        @JsonProperty("reason") String reason,
        @JsonProperty("state") RelistingState state
    ) {
        this.reason = reason;
        this.state = state;
    }
}
