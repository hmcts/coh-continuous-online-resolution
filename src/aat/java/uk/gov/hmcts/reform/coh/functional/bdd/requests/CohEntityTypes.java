package uk.gov.hmcts.reform.coh.functional.bdd.requests;

public enum CohEntityTypes {
    ONLINE_HEARING,
    QUESTION,
    DECISION,
    DECISION_REPLY;

    public String getString() {
        return name().replaceAll("_", " ").toLowerCase();
    }
}
