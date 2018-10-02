package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.domain.RelistingState;

import java.util.Date;

public class RelistingResponse {
    public final String reason;
    public final RelistingState state;
    public final Date created;
    public final Date updated;

    public RelistingResponse(
        @JsonProperty("reason") String reason,
        @JsonProperty("state") RelistingState state,
        @JsonProperty Date created,
        @JsonProperty Date updated
    ) {
        this.reason = reason;
        this.state = state;
        this.created = created;
        this.updated = updated;
    }
}
