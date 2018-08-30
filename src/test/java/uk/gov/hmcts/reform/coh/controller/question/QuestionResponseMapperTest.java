package uk.gov.hmcts.reform.coh.controller.question;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QuestionResponseMapperTest {

    private UUID questionUuid;

    private QuestionState state;

    private Question question;

    @Before
    public void setup() {

        state = new QuestionState();
        state.setState("foo");

        question = QuestionEntityUtils.createTestQuestion();
        questionUuid = question.getQuestionId();

        AnswerState answerState = new AnswerState();
        answerState.setState("foo");
        Answer answer = new Answer();
        answer.answerText("foo").answerId(UUID.randomUUID());
        answer.setAnswerState(answerState);
        question.setAnswers(Arrays.asList(answer));
    }

    @Test
    public void testResponseMappings() {

        Calendar yesterday = new GregorianCalendar();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        List<QuestionStateHistory> histories = new ArrayList<>();
        QuestionStateHistory history1 = new QuestionStateHistory(question, state);
        history1.setDateOccurred(yesterday.getTime());

        Date today = new Date();
        QuestionStateHistory history2 = new QuestionStateHistory(question, state);
        history2.setDateOccurred(today);

        histories.add(history1);
        histories.add(history2);
        question.setQuestionStateHistories(histories);

        QuestionResponse response = new QuestionResponse();
        QuestionResponseMapper.map(question, response);

        // Check each field is mapped correctly
        assertEquals(questionUuid.toString(), response.getQuestionId());
        assertEquals("1", response.getQuestionRound());
        assertEquals("1", response.getQuestionOrdinal());
        assertEquals(question.getQuestionHeaderText(), response.getQuestionHeaderText());
        assertEquals(question.getQuestionText(), response.getQuestionBodyText());
        assertEquals(question.getOwnerReferenceId(), response.getOwnerReference());
        assertEquals(QuestionStates.DRAFTED.getStateName(), response.getCurrentState().getName());
        assertEquals(1, question.getAnswers().size());
        assertEquals("1", response.getDeadlineExtCount());

        // This checks the sorting works
        assertEquals(CohISO8601DateFormat.format(history2.getDateOccurred()), response.getCurrentState().getDatetime());
    }

    @Test
    public void testMappingsNullDecisionStateHistories() {

        QuestionResponse response = new QuestionResponse();
        QuestionResponseMapper.map(question, response);

        // This checks the sorting works
        assertNull(response.getCurrentState().getDatetime());
    }
}