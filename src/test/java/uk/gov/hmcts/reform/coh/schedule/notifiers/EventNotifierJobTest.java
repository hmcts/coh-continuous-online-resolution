package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;
import uk.gov.hmcts.reform.coh.service.SessionEventService;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates.*;
import static uk.gov.hmcts.reform.coh.events.EventTypes.*;

@RunWith(SpringRunner.class)
public class EventNotifierJobTest {

    @Mock
    private SessionEventService sessionEventService;

    @Mock
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    @Mock
    private EventTransformerFactory factory;

    @Mock
    @Qualifier("BasicJsonNotificationForwarder")
    private NotificationForwarder forwarder;

    @InjectMocks
    private EventNotifierJob job;

    private SessionEventForwardingState pendingState;

    private SessionEventForwardingState successState;

    private SessionEventForwardingRegister register;

    private SessionEvent sessionEvent;

    private NotificationRequest request;

    @Before
    public void setUp() throws NotificationException {

        pendingState = new SessionEventForwardingState();
        pendingState.setForwardingStateName(EVENT_FORWARDING_PENDING.getStateName());

        successState = new SessionEventForwardingState();
        successState.setForwardingStateName(EVENT_FORWARDING_SUCCESS.getStateName());

        SessionEventType sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(DECISION_ISSUED.getEventType());

        register = new SessionEventForwardingRegister();
        register.setForwardingEndpoint("http://www.foo.com");
        register.setSessionEventType(sessionEventType);

        sessionEvent = new SessionEvent();
        sessionEvent.setSessionEventForwardingState(pendingState);
        sessionEvent.setEventId(UUID.randomUUID());
        sessionEvent.setSessionEventForwardingRegister(register);

        EventTransformer transformer = (s, o) -> request;

        ResponseEntity okResponse = new ResponseEntity(HttpStatus.OK);

        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_PENDING.getStateName())).willReturn(Optional.of(pendingState));
        given(sessionEventService.retrieveBySessionEventForwardingState(pendingState)).willReturn(Arrays.asList(sessionEvent));
        given(factory.getEventTransformer(DECISION_ISSUED.getEventType())).willReturn(transformer);
        given(forwarder.sendEndpoint(register, request)).willReturn(okResponse);
    }

    @Test
    public void testNoPendingState() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_PENDING.getStateName())).willReturn(Optional.empty());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testNoSessionEventsInPendingState() {
        given(sessionEventService.retrieveBySessionEventForwardingState(pendingState)).willReturn(Arrays.asList());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testNoEventTransformerForSessionEvents() {
        given(factory.getEventTransformer(DECISION_ISSUED.getEventType())).willReturn(null);
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testHttpFailure() throws NotificationException {
        ResponseEntity failureRespnse = new ResponseEntity(HttpStatus.NOT_FOUND);
        given(forwarder.sendEndpoint(register, request)).willReturn(failureRespnse);
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testNoSessionEventsInSuccessState() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_SUCCESS.getStateName())).willReturn(Optional.empty());
        job.execute();
        assertEquals(EVENT_FORWARDING_PENDING.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }

    @Test
    public void testSuccess() {
        given(sessionEventForwardingStateRepository.findByForwardingStateName(EVENT_FORWARDING_SUCCESS.getStateName())).willReturn(Optional.of(successState));
        job.execute();
        assertEquals(EVENT_FORWARDING_SUCCESS.getStateName(), sessionEvent.getSessionEventForwardingState().getForwardingStateName());
    }
}
