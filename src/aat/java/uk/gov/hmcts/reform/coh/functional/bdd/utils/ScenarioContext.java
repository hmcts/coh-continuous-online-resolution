package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.UpdateDecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.AllDecisionRepliesResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.UpdateOnlineHearingRequest;
import uk.gov.hmcts.reform.coh.controller.question.UpdateQuestionRequest;
import uk.gov.hmcts.reform.coh.domain.*;

import java.util.*;

@Component
public class ScenarioContext {

    private OnlineHearingRequest currentOnlineHearingRequest;

    private OnlineHearing currentOnlineHearing;

    private Question currentQuestion;

    private Answer currentAnswer;

    private Jurisdiction currentJurisdiction;

    private DecisionRequest currentDecisionRequest;

    private UpdateDecisionRequest updateDecisionRequest;

    private UpdateOnlineHearingRequest updateOnlineHearingRequest;

    private Decision currentDecision;

    private List<String> caseIds;

    private List<UUID> answerIds;

    private List<UUID> questionIds;

    private UpdateQuestionRequest updateQuestionRequest;
    private DecisionReplyRequest decisionReplyRequest;

    private List<Jurisdiction> jurisdictions;
    private List<DecisionReply> decisionReplies = new ArrayList<>();
    private EventRegistrationRequest eventRegistrationRequest;
    private AllDecisionRepliesResponse allDecisionRepliesResponse;
    private DecisionReplyResponse decisionReplyResponse;

    private Set<SessionEventForwardingRegister> sessionEventForwardingRegisters = new HashSet<>();

    public Set<SessionEventForwardingRegister> getSessionEventForwardingRegisters() {
        return sessionEventForwardingRegisters;
    }

    public void setSessionEventForwardingRegisters(
        Set<SessionEventForwardingRegister> sessionEventForwardingRegisters) {
        this.sessionEventForwardingRegisters = sessionEventForwardingRegisters;
    }

    public void addSessionEventForwardingRegister(SessionEventForwardingRegister sessionEventForwardingRegister) {
        sessionEventForwardingRegisters.add(sessionEventForwardingRegister);
    }

    public OnlineHearingRequest getCurrentOnlineHearingRequest() {
        return currentOnlineHearingRequest;
    }

    public UpdateQuestionRequest getUpdateQuestionRequest() {
        return updateQuestionRequest;
    }

    public void setUpdateQuestionRequest(UpdateQuestionRequest updateQuestionRequest) {
        this.updateQuestionRequest = updateQuestionRequest;
    }

    public void setCurrentOnlineHearingRequest(OnlineHearingRequest currentOnlineHearingRequest) {
        this.currentOnlineHearingRequest = currentOnlineHearingRequest;
    }

    public OnlineHearing getCurrentOnlineHearing() {
        return currentOnlineHearing;
    }

    public void setCurrentOnlineHearing(OnlineHearing currentOnlineHearing) {
        this.currentOnlineHearing = currentOnlineHearing;
    }

    public void setCurrentOnlineHearing(OnlineHearingRequest onlineHearingRequest) {
        currentOnlineHearing = new OnlineHearing();
        currentOnlineHearing.setCaseId(onlineHearingRequest.getCaseId());
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public Answer getCurrentAnswer() {
        return currentAnswer;
    }

    public void setCurrentAnswer(Answer currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    public Jurisdiction getCurrentJurisdiction() {
        return currentJurisdiction;
    }

    public void setCurrentJurisdiction(Jurisdiction currentJurisdiction) {
        this.currentJurisdiction = currentJurisdiction;
    }

    public DecisionRequest getCurrentDecisionRequest() {
        return currentDecisionRequest;
    }

    public void setCurrentDecisionRequest(DecisionRequest currentDecisionRequest) {
        this.currentDecisionRequest = currentDecisionRequest;
    }

    public UpdateDecisionRequest getUpdateDecisionRequest() {
        return updateDecisionRequest;
    }

    public void setUpdateDecisionRequest(UpdateDecisionRequest updateDecisionRequest) {
        this.updateDecisionRequest = updateDecisionRequest;
    }

    public Decision getCurrentDecision() {
        return currentDecision;
    }

    public void setCurrentDecision(Decision currentDecision) {
        this.currentDecision = currentDecision;
    }

    public List<String> getCaseIds() {
        return caseIds;
    }

    public void setCaseIds(List<String> caseId) {
        this.caseIds = caseId;
    }

    public void addCaseId(String caseId) {
        if (caseIds == null) {
            caseIds = new ArrayList<>();
        }
        caseIds.add(caseId);
    }

    public List<UUID> getAnswerIds() {
        return answerIds;
    }

    public void addAnswerId(UUID answerId) {
        if (answerIds == null) {
            answerIds = new ArrayList<>();
        }
        answerIds.add(answerId);
    }

    public List<UUID> getQuestionIds() {
        return questionIds;
    }

    public void addQuestionId(UUID questionId) {
        if (questionIds == null) {
            questionIds = new ArrayList<>();
        }
        questionIds.add(questionId);
    }

    public List<Jurisdiction> getJurisdictions() {
        return jurisdictions;
    }

    public void addJurisdiction(Jurisdiction jurisdiction) {
        if (jurisdictions == null) {
            jurisdictions = new ArrayList<>();
        }

        jurisdictions.add(jurisdiction);
    }

    public void clear() {
        currentOnlineHearing = null;
        currentQuestion = null;
        currentAnswer = null;
        caseIds = null;
        jurisdictions = null;
        updateQuestionRequest = null;
        eventRegistrationRequest = null;
        sessionEventForwardingRegisters = null;
        decisionReplyRequest = null;
        answerIds = null;
    }

    public void clearDecisionReplies() {
        decisionReplies = new ArrayList<>();
    }

    public UpdateOnlineHearingRequest getUpdateOnlineHearingRequest() {
        return updateOnlineHearingRequest;
    }

    public void setUpdateOnlineHearingRequest(UpdateOnlineHearingRequest updateOnlineHearingRequest) {
        this.updateOnlineHearingRequest = updateOnlineHearingRequest;
    }

    public void setEventRegistrationRequest(EventRegistrationRequest eventRegistrationRequest) {
        this.eventRegistrationRequest = eventRegistrationRequest;
    }

    public EventRegistrationRequest getEventRegistrationRequest() {
        return eventRegistrationRequest;
    }

    public void setCurrentDecisionReplyRequest(DecisionReplyRequest decisionReplyRequest) {
        this.decisionReplyRequest = decisionReplyRequest;
    }

    public DecisionReplyRequest getCurrentDecisionReplyRequest() {
        return decisionReplyRequest;
    }

    public void addDecisionReply(DecisionReply decisionReply) {

        decisionReplies.add(decisionReply);
    }

    public void setDecisionReplies(List<DecisionReply> decisionReplies) {

        this.decisionReplies = decisionReplies;
    }

    public List<DecisionReply> getDecisionReplies() {
        return decisionReplies;
    }

    public void setAllDecisionRepliesResponse(AllDecisionRepliesResponse allDecisionRepliesResponse) {
        this.allDecisionRepliesResponse = allDecisionRepliesResponse;
    }

    public AllDecisionRepliesResponse getAllDecisionRepliesResponse() {
        return allDecisionRepliesResponse;
    }

    public void setDecisionReplyResponse(DecisionReplyResponse decisionReplyResponse) {
        this.decisionReplyResponse = decisionReplyResponse;
    }

    public DecisionReplyResponse getDecisionReplyResponse() {
        return decisionReplyResponse;
    }
}
