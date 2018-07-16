package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotAValidUpdateException;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionStateService questionStateService;

    @Mock
    private QuestionRoundService questionRoundService;

    private QuestionService questionService;

    private QuestionState drafted = new QuestionState("question_drafted");
    private QuestionState issued = new QuestionState("question_issued");
    private Question question;

    private OnlineHearing onlineHearing;
    private static UUID ONE;

    @Before
    public void setup() {
        ONE = UUID.randomUUID();
        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(ONE);

        questionService = new QuestionService(questionRepository, questionStateService, questionRoundService);
        QuestionState issuedState = new QuestionState();
        issuedState.setQuestionStateId(3);
        issuedState.setState("ISSUED");
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.ofNullable(issuedState));
        given(questionRoundService.isQrValidTransition(any(Question.class), any(OnlineHearing.class))).willReturn(true);
        given(questionRoundService.isQrValidState(any(Question.class), any(OnlineHearing.class))).willReturn(true);
        question = new Question();
    }

    @Test
    public void testCreateQuestion() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateByStateName("question_drafted")).thenReturn(Optional.ofNullable(drafted));

        /**
         * This needs to be fixed so that online hearing id is an attribute of question
         */
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(ONE);
        Question newQuestion = questionService.createQuestion(question, onlineHearing);
        verify(questionStateService, times(1)).retrieveQuestionStateByStateName("question_drafted");
        assertEquals(newQuestion, question);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testCreateQuestionWithInvalidUpdate() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);
        given(questionRoundService.isQrValidTransition(any(Question.class), any(OnlineHearing.class))).willReturn(false);

        questionService.createQuestion(question, onlineHearing);
    }

    @Test
    public void testRetrieveQuestion() {
        when(questionRepository.findById(ONE)).thenReturn(Optional.of(question));
        when(questionStateService.retrieveQuestionStateById(1)).thenReturn(drafted);

        Optional<Question> newQuestion = questionService.retrieveQuestionById(ONE);
        verify(questionRepository, times(1)).findById(ONE);
        assertEquals(question, newQuestion.get());
    }

    @Test
    public void testEditQuestion() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionRepository.findById(ONE)).thenReturn(Optional.of(question));
        when(questionStateService.retrieveQuestionStateByStateName("question_issued")).thenReturn(Optional.ofNullable(issued));

        /**
         * This needs to be fixed so that question id is an attribute of question
         */
        Question newQuestion = questionService.editQuestion(ONE, question);
        verify(questionRepository, times(1)).findById(ONE);
        assertEquals("Correct state", issued, newQuestion.getQuestionState());
        assertEquals("Event logged", 1, newQuestion.getQuestionStateHistories().size());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testEditQuestionWithInvalidQuestionId() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionRepository.findById(ONE)).thenReturn(Optional.of(question));
        when(questionStateService.retrieveQuestionStateById(3)).thenReturn(issued);
        when(questionRepository.findById(ONE)).thenReturn(Optional.empty());
        /**
         * This needs to be fixed so that question id is an attribute of question
         */
        questionService.editQuestion(ONE, question);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testEditQuestionUnknownState() {
        when(questionRepository.save(question)).thenReturn(question);
        when(questionRepository.findById(ONE)).thenReturn(Optional.of(question));
        when(questionStateService.retrieveQuestionStateByStateName(anyString())).thenReturn(Optional.empty());
        questionService.editQuestion(ONE, question);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateWhenStateNotFound() {

        // Pretend issued is not a valid state
        when(questionStateService.retrieveQuestionStateByStateName(anyString())).thenReturn(Optional.empty());
        Question question = new Question();
        question.setQuestionState(drafted);

        Question body = new Question();
        body.setQuestionState(issued);
        questionService.updateQuestion(question, body);
    }

    @Test
    public void testDelete() {
        doNothing().when(questionRepository).delete(question);
        questionService.deleteQuestion(question);
        verify(questionRepository, times(1)).delete(question);
    }

    @Test(expected = NotAValidUpdateException.class)
    public void testUserCanNotUpdateQuestionToIssued() {
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(issued));
        Question question = new Question();
        question.setQuestionState(drafted);

        Question body = new Question();
        body.setQuestionState(issued);
        questionService.updateQuestion(question, body);
    }

    @Test
    public void testFindAllQuestionsByOnlineHearing() {
        List<Question> questions = new ArrayList<>();
        questions.add(question);

        given(questionRepository.findAllByOnlineHearing(onlineHearing)).willReturn(questions);
        Optional<List<Question>> responses = questionService.finaAllQuestionsByOnlineHearing(onlineHearing);

        assertTrue(responses.isPresent());
        assertEquals(1, responses.get().size());
    }

    @Test
    public void testFindAllQuestionsByOnlineHearingNone() {
        given(questionRepository.findAllByOnlineHearing(onlineHearing)).willReturn(null);
        Optional<List<Question>> responss = questionService.finaAllQuestionsByOnlineHearing(onlineHearing);

        assertFalse(responss.isPresent());
    }
}
