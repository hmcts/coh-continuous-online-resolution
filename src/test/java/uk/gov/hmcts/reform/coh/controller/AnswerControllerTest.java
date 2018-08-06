package uk.gov.hmcts.reform.coh.controller;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.AnswerStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.task.AnswersReceivedTask;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class AnswerControllerTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionService questionService;

    @Mock
    private AnswerService answerService;

    @Mock
    private AnswerStateService answerStateService;

    @Mock
    private AnswersReceivedTask answersReceivedTask;

    @Mock
    private SessionEventService sessionEventService;

    @InjectMocks
    private AnswerController answerController;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/continuous-online-hearings/d9248584-4aa5-4cb0-aba6-d2633ad5a375/questions/d9248584-4aa5-4cb0-aba6-d2633ad5a375/answers";

    private AnswerRequest request;

    private Answer answer;

    private UUID uuid;

    private OnlineHearing onlineHearing;

    private AnswerState draftedState;

    private AnswerState submittedState;

    private Question question;

    private QuestionState questionState;

    @Before
    public void setup() throws IOException, NotFoundException {
        answer = new Answer();
        uuid = UUID.fromString("399388b4-7776-40f9-bb79-0e900807063b");
        answer.answerId(uuid).answerText("foo");

        draftedState = new AnswerState();
        draftedState.setState(AnswerStates.DRAFTED.getStateName());
        draftedState.setAnswerStateId(1);
        answer.setAnswerState(draftedState);

        submittedState = new AnswerState();
        submittedState.setState(AnswerStates.SUBMITTED.getStateName());
        submittedState.setAnswerStateId(2);

        mockMvc = MockMvcBuilders.standaloneSetup(answerController).build();

        questionState = new QuestionState();
        questionState.setState(QuestionStates.ISSUED.getStateName());
        question = new Question();
        question.setQuestionState(questionState);

        onlineHearing = new OnlineHearing();


        given(sessionEventService.createSessionEvent(any(OnlineHearing.class), anyString())).willReturn(new SessionEvent());
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.of(question));
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.ofNullable(answer));
        given(answerService.createAnswer(any(Answer.class))).willReturn(answer);
        given(answerService.updateAnswer(any(Answer.class), any(Answer.class))).willReturn(answer);
        given(answerStateService.retrieveAnswerStateByState(draftedState.getState())).willReturn(Optional.ofNullable(draftedState));
        given(answerStateService.retrieveAnswerStateByState(submittedState.getState())).willReturn(Optional.ofNullable(submittedState));
        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).willReturn(Optional.ofNullable(onlineHearing));
        request = (AnswerRequest) JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
    }

    @Test
    public void testCreateAnswerOnlineHearingNotFound() throws Exception {

        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testEmptyAnswerText() throws Exception {

        request.setAnswerText(null);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testEmptyAnswerTextForPatchThrowsRequestError() throws Exception {

        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testEmptyAnswerState() throws Exception {
        AnswerRequest request = (AnswerRequest) JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        request.setAnswerState(null);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testInvalidAnswerState() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        // Just pretend that DRAFTED isn't a valid state
        given(answerStateService.retrieveAnswerStateByState(AnswerStates.DRAFTED.getStateName())).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testCreateAnswerAndCheckHeaderForLocation() throws Exception {

        AnswerRequest request = (AnswerRequest)JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        request.setAnswerState(AnswerStates.SUBMITTED.getStateName());

        mockSubmittedAnswer();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String returnedUrl = result.getResponse().getHeader("Location");
        try {
            URL u = new URL(returnedUrl); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            assertTrue(true);
            verify(sessionEventService, times(1)).createSessionEvent(any(OnlineHearing.class), anyString());
        }catch(MalformedURLException e){
            fail();
        }
    }

    @Test
    public void testCreateAnswerQuestionNotFound() throws Exception {

        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.empty());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("The question does not exist or has not yet been issued", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateAnswerInvalidOnlineHearing() throws Exception {

        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateMultipleAnswer() throws Exception {

        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswersByQuestion(any(Question.class))).willReturn(Arrays.asList(answer));
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateAnswerDrafted() throws Exception {
        questionState = new QuestionState();
        questionState.setState(QuestionStates.ISSUE_PENDING.getStateName());
        question = new Question();
        question.setQuestionState(questionState);

        String json = JsonUtils.getJsonInput("answer/standard_answer");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(HttpStatus.CREATED.value(),result.getResponse().getStatus());
        assertEquals(AnswerStates.DRAFTED.getStateName(), answer.getAnswerState().getState());
        verify(sessionEventService, times(0)).createSessionEvent(any(OnlineHearing.class), anyString());
        verify(answersReceivedTask, times(0)).execute(any(OnlineHearing.class));
    }

    @Test
    public void testCreateAnswerSubmitted() throws Exception {
        questionState = new QuestionState();
        questionState.setState(QuestionStates.ISSUE_PENDING.getStateName());
        question = new Question();
        question.setQuestionState(questionState);

        AnswerRequest request = (AnswerRequest)JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        request.setAnswerState(AnswerStates.SUBMITTED.getStateName());

        mockSubmittedAnswer();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(HttpStatus.CREATED.value(),result.getResponse().getStatus());
        assertEquals(AnswerStates.SUBMITTED.getStateName(), answer.getAnswerState().getState());
        verify(sessionEventService, times(1)).createSessionEvent(any(OnlineHearing.class), anyString());
        verify(answersReceivedTask, times(1)).execute(any(OnlineHearing.class));
    }

    @Test
    public void testGetAnswer() throws Exception {

        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.of(answer));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertEquals("{\"answer_id\":\"" + uuid +"\",\"answer_text\":\"foo\",\"current_answer_state\":{\"state_name\":\"answer_drafted\"}}", response);
        AnswerResponse getAnswer = (AnswerResponse) JsonUtils.toObjectFromJson(response, AnswerResponse.class);
        assertEquals(uuid.toString(), getAnswer.getAnswerId());
        assertEquals("foo", getAnswer.getAnswerText());
        assertEquals("answer_drafted", getAnswer.getStateResponse().getName());
    }

    @Test
    public void testGetAnswersQuestionNotFound() throws Exception {

        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isFailedDependency());
    }

    @Test
    public void testGetAnswers() throws Exception {

        Question question = new Question();
        question.setQuestionId(UUID.randomUUID());
        Answer answer = new Answer();
        answer.answerId(uuid).answerText("foo");
        answer.setAnswerState(draftedState);
        List<Answer> answerList = new ArrayList<>();
        answerList.add(answer);
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.of(question));
        given(answerService.retrieveAnswersByQuestion(question)).willReturn(answerList);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AnswerResponse[] answers = (AnswerResponse[]) JsonUtils.toObjectFromJson(response, AnswerResponse[].class);
        assertEquals(1, answers.length);
    }

    @Test
    public void testUpdateAnswersOnlineHearingNotFound() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateAnswersDraft() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.of(answer));

        doNothing().when(answersReceivedTask).execute(any(OnlineHearing.class));
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
        verify(sessionEventService, times(0)).createSessionEvent(any(OnlineHearing.class), anyString());
    }

    @Test
    public void testUpdateAnswersSubmitted() throws Exception {
        AnswerRequest request = (AnswerRequest)JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
        request.setAnswerState(AnswerStates.SUBMITTED.getStateName());

        Answer savedAnswer = new Answer();
        savedAnswer.setAnswerId(UUID.randomUUID());
        savedAnswer.setAnswerState(submittedState);
        given(answerService.updateAnswer(any(Answer.class), any(Answer.class))).willReturn(savedAnswer);
        doNothing().when(answersReceivedTask).execute(any(OnlineHearing.class));
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request)))
                .andExpect(status().isOk());
        verify(sessionEventService, times(1)).createSessionEvent(any(OnlineHearing.class), anyString());
        verify(answersReceivedTask, times(1)).execute(any(OnlineHearing.class));
    }

    @Test
    public void testUpdateAnswersFail() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testUpdateAnswersNonExistentQuestion() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateAnswersWhenAnswerAlreadySubmitted() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        draftedState.setState(AnswerStates.SUBMITTED.getStateName());
        answer.setAnswerState(draftedState);
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.of(answer));

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testUpdateAnswersFailDueToAnswerStateNotFoundThrowException() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerStateService.retrieveAnswerStateByState(anyString())).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testUpdateAnswersFailDueToInvalidStateTransition() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerService.updateAnswer(any(Answer.class), any(Answer.class))).willThrow(NotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
    }

    @Test
    public void testUpdateAnswersInvalidAnswerState() throws Exception {
        String json = JsonUtils.getJsonInput("answer/standard_answer");
        given(answerStateService.retrieveAnswerStateByState(anyString())).willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        assertEquals("Answer state is not valid: answer_drafted", result.getResponse().getContentAsString());
    }

    private void mockSubmittedAnswer() {
        // Sets the mock to created
        answer.setAnswerId(UUID.randomUUID());
        answer.setAnswerState(submittedState);
    }
}