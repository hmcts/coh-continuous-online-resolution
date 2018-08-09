package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.*;

@Entity
@Table(name = "online_hearing_panel_member")
public class OnlineHearingPanelMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "online_hearing_panel_id", nullable = false)
    private Long onlineHearingPanelId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "online_hearing_id")
    private OnlineHearing onlineHearing;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "identity_reference")
    private String identityToken;

    @Column(name = "role")
    private String role;

    public Long getOnlineHearingPanelId() {
        return onlineHearingPanelId;
    }

    public void setOnlineHearingPanelId(Long onlineHearingPanelId) {
        this.onlineHearingPanelId = onlineHearingPanelId;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdentityToken() {
        return identityToken;
    }

    public void setIdentityToken(String identityToken) {
        this.identityToken = identityToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}