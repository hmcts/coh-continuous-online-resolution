package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OnlineHearingResponse implements Serializable {

    @JsonProperty("online_hearing_id")
    private UUID onlineHearingId;

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("start_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date startDate;

    @JsonProperty(value = "end_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endDate;

    @JsonProperty("panel")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PanelMember> panel;

    @JsonProperty("current_state")
    private CurrentState currentState;

    public UUID getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(UUID onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<PanelMember> getPanel() {
        return panel;
    }

    public void setPanel(List<PanelMember> panel) {
        this.panel = panel;
    }

    public CurrentState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CurrentState currentState) {
        this.currentState = currentState;
    }

    public static class PanelMember {
        @JsonProperty("name")
        private String name;

        public PanelMember() {
            super();
        }

        public PanelMember(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class CurrentState {

        @JsonProperty("state_name")
        private String state_name;

        @JsonProperty("state_datetime")
        private String datetime;

        public CurrentState(String state, String datetime) {
            this.state_name = state;
            this.datetime = datetime;
        }

        public String getState_name() {
            return state_name;
        }

        public void setState_name(String state_name) {
            this.state_name = state_name;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }
    }
}
