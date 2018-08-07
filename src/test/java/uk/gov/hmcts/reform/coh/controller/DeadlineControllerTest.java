package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.coh.controller.state.DeadlineExtensionHelper;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.service.exceptions.NoQuestionsAsked;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class DeadlineControllerTest {

    @MockBean
    private OnlineHearingService onlineHearingService;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private SessionEventService sessionEventService;

    @Autowired
    private MockMvc mockMvc;

    private DeadlineExtensionHelper helper;

    @Before
    public void setUp() throws NoQuestionsAsked {
        helper = new DeadlineExtensionHelper(0, 0, 0);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
    }

    @Test
    public void testOnlineHearingNotFound() throws Exception {
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(Optional.empty());

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testNoGrantedNoDenied() throws Exception {
        helper = new DeadlineExtensionHelper(1, 0, 0);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().isOk());
    }

    @Test
    public void tesDeniedAndSessionEventQueued() throws Throwable {
        helper = new DeadlineExtensionHelper(1, 0, 1);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().isOk());
        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing.get(), EventTypes.QUESTION_DEADLINE_EXTENSION_DENIED.getEventType());
    }

    @Test
    public void testGrantedAndSessionEventQueued() throws Throwable {
        helper = new DeadlineExtensionHelper(1, 1, 0);
        when(questionService.requestDeadlineExtension(any())).thenReturn(helper);
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
                .andExpect(status().isOk());
        verify(sessionEventService, times(1)).createSessionEvent(onlineHearing.get(), EventTypes.QUESTION_DEADLINE_EXTENSION_GRANTED.getEventType());
    }

    @Test
    public void testSuccessfulExtensionRequest() throws Exception {
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
            .andExpect(status().isOk());
    }

    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    @Test
    public void testExtensionRequestWithExceptionThrown() throws Exception, NoQuestionsAsked {
        OnlineHearing spyOnlineHearing = spy(OnlineHearing.class);
        Optional<OnlineHearing> onlineHearing = Optional.of(spyOnlineHearing);
        when(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).thenReturn(onlineHearing);

        doThrow(RuntimeException.class).when(questionService).requestDeadlineExtension(any());

        UUID onlineHearingId = UUID.randomUUID();
        mockMvc.perform(put("/continuous-online-hearings/" + onlineHearingId + "/questions-deadline-extension"))
            .andExpect(status().is5xxServerError());
    }
}
