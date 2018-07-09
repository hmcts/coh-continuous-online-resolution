package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "decision")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "decision_id")
    private UUID decisionId;

    @OneToOne(optional=false)
    @JoinColumn(name = "online_hearing_id")
    private OnlineHearing onlineHearing;

    @Column(name = "decision_header")
    private String decisionHeader;

    @Column(name = "decision_text", length = 2000)
    private String decisionText;

    @Column(name = "decision_reason", length = 2000)
    private String decisionReason;

    @Column(name = "decision_award")
    private String decisionAward;

    @OneToOne(optional=false)
    @JoinColumn(name = "decision_state_id")
    private DecisionState decisionstate;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deadline_expiry_date")
    private Date deadlineExpiryDate = new Date();

    @Column(name = "author_reference_id ")
    private String authorReferenceId ;

    @Column(name = "owner_reference_id  ")
    private String ownerReferenceId ;

    public UUID getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(UUID decisionId) {
        this.decisionId = decisionId;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public String getDecisionHeader() {
        return decisionHeader;
    }

    public void setDecisionHeader(String decisionHeader) {
        this.decisionHeader = decisionHeader;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public void setDecisionText(String decisionText) {
        this.decisionText = decisionText;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getDecisionAward() {
        return decisionAward;
    }

    public void setDecisionAward(String decisionAward) {
        this.decisionAward = decisionAward;
    }

    public DecisionState getDecisionstate() {
        return decisionstate;
    }

    public void setDecisionstate(DecisionState decisionstate) {
        this.decisionstate = decisionstate;
    }

    public Date getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public void setDeadlineExpiryDate(Date deadlineExpiryDate) {
        this.deadlineExpiryDate = deadlineExpiryDate;
    }

    public String getAuthorReferenceId() {
        return authorReferenceId;
    }

    public void setAuthorReferenceId(String authorReferenceId) {
        this.authorReferenceId = authorReferenceId;
    }

    public String getOwnerReferenceId() {
        return ownerReferenceId;
    }

    public void setOwnerReferenceId(String ownerReferenceId) {
        this.ownerReferenceId = ownerReferenceId;
    }
}
