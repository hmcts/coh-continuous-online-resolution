package uk.gov.hmcts.reform.coh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.*;

import static javax.persistence.FetchType.EAGER;

@Entity(name = "Question")
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_id")
    private UUID questionId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "online_hearing_id")
    @JsonIgnore
    private OnlineHearing onlineHearing;

    @Column(name = "question_ordinal")
    private int questionOrdinal;

    @Column(name = "question_header_text", length = 5000)
    private String questionHeaderText;

    @Column(name = "question_text", columnDefinition="CLOB NOT NULL")
    @Lob
    private String questionText;

    @Column(name = "question_round")
    private Integer questionRound;

    @ElementCollection
    @CollectionTable(name = "linked_question",
            joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "linked_question_id")
    private Set<UUID> linkedQuestions;

    @Column(name = "deadline_expiry_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deadlineExpiryDate;

    @Column(name = "deadline_ext_count")
    private int deadlineExtCount;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "question_state_id")
    private QuestionState questionState;

    @Column(name = "author_reference_id")
    private String authorReferenceId;

    @Column(name = "owner_reference_id")
    private String ownerReferenceId ;

    @OneToMany(
            fetch = EAGER,
            mappedBy = "question",
            cascade = CascadeType.ALL
    )
    private List<QuestionStateHistory> questionStateHistories = new ArrayList<>();

    @Transient
    private List<Answer> answers;

    public Question() {}

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public int getQuestionOrdinal() {
        return questionOrdinal;
    }

    public void setQuestionOrdinal(int questionOrdinal) {
        this.questionOrdinal = questionOrdinal;
    }

    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    public void setQuestionHeaderText(String questionHeaderText) {
        this.questionHeaderText = questionHeaderText;
    }

    public Set<UUID> getLinkedQuestions() {
        return linkedQuestions;
    }

    public void setLinkedQuestions(Set<UUID> linkedQuestions) {
        this.linkedQuestions = linkedQuestions;
    }

    public Date getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public void setDeadlineExpiryDate(Date deadlineExpiryDate) {
        this.deadlineExpiryDate = deadlineExpiryDate;
    }

    public int getDeadlineExtCount() {
        return deadlineExtCount;
    }

    public void setDeadlineExtCount(int deadlineExtCount) {
        this.deadlineExtCount = deadlineExtCount;
    }

    public void incrementDeadlineExtCount() {
        this.deadlineExtCount += 1;
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

    public List<QuestionStateHistory> getQuestionStateHistories() {
        return questionStateHistories;
    }

    public void setQuestionStateHistories(List<QuestionStateHistory> questionStateHistories) {
        this.questionStateHistories = questionStateHistories;
    }

    public void updateQuestionStateHistory(QuestionState state) {
        questionStateHistories.add(new QuestionStateHistory(this, state));
    }

    public Integer getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRound(Integer questionRound) {
        this.questionRound = questionRound;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionState getQuestionState() {
        return questionState;
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }

    public OnlineHearing getOnlineHearing() {
        return onlineHearing;
    }

    public void setOnlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Question question = (Question) o;
        return Objects.equals(questionId, question.questionId);
    }

    public Question questionId(UUID questionId) {
        this.questionId = questionId;
        return this;
    }

    public Question onlineHearing(OnlineHearing onlineHearing) {
        this.onlineHearing = onlineHearing;
        return this;
    }

    public Question questionOrdinal(int questionOrdinal) {
        this.questionOrdinal = questionOrdinal;
        return this;
    }

    public Question questionHeaderText(String questionHeaderText) {
        this.questionHeaderText = questionHeaderText;
        return this;
    }

    public Question questionText(String questionText) {
        this.questionText = questionText;
        return this;
    }

    public Question questionRound(int questionRound) {
        this.questionRound = questionRound;
        return this;
    }

    public Question linkedQuestions(Set<UUID> linkedQuestions) {
        this.linkedQuestions = linkedQuestions;
        return this;
    }

    public Question deadlineExpiryDate(Date deadlineExpiryDate) {
        this.deadlineExpiryDate = deadlineExpiryDate;
        return this;
    }

    public Question questionState(QuestionState questionState) {
        this.questionState = questionState;
        return this;
    }

    public Question authorReferenceId(String authorReferenceId) {
        this.authorReferenceId = authorReferenceId;
        return this;
    }

    public Question ownerReferenceId(String ownerReferenceId) {
        this.ownerReferenceId = ownerReferenceId;
        return this;
    }

    public Question questionStateHistories(List<QuestionStateHistory> questionStateHistories) {
        this.questionStateHistories = questionStateHistories;
        return this;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
}