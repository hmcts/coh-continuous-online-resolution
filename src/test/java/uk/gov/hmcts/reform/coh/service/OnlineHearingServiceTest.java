package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.*;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class OnlineHearingServiceTest {

    @Mock
    private OnlineHearingRepository onlineHearingRepository;

    private OnlineHearingService onlineHearingService;

    private OnlineHearing createdOnlineHearing;

    private OnlineHearingState onlineHearingState;

    @Before
    public void setup() {
        onlineHearingService = new OnlineHearingService(onlineHearingRepository);
        createdOnlineHearing = new OnlineHearing();
        createdOnlineHearing.setOnlineHearingId(randomUUID());
        onlineHearingState = new OnlineHearingState();
        onlineHearingState.setState("state");
    }

    @Test
    public void testCreateOnlineHearing() {
        when(onlineHearingRepository.save(createdOnlineHearing)).thenReturn(createdOnlineHearing);
        OnlineHearing newOnlineHearing = onlineHearingService.createOnlineHearing(createdOnlineHearing);
        assertEquals(createdOnlineHearing, newOnlineHearing);
    }

    @Test
    public void testRetrieveOnlineHearing() {
        when(onlineHearingRepository.findById(any(UUID.class))).thenReturn(Optional.of(createdOnlineHearing));
        Optional<OnlineHearing> newOnlineHearing = onlineHearingService.retrieveOnlineHearing(createdOnlineHearing);
        assertTrue(newOnlineHearing.isPresent());
        assertEquals(createdOnlineHearing, newOnlineHearing.get());
    }

    @Test
    public void testRetrieveOnlineHearingByCaseId() {
        createdOnlineHearing.setCaseId("foo");
        when(onlineHearingRepository.findByCaseId(any(String.class))).thenReturn(Optional.of(createdOnlineHearing));
        OnlineHearing newOnlineHearing = onlineHearingService.retrieveOnlineHearingByCaseId(createdOnlineHearing);
        assertEquals(createdOnlineHearing, newOnlineHearing);
    }

    @Test
    public void testRetrieveOnlineHearingByCaseIds() {
        List<String> caseId = Arrays.asList("foo", "bar");
        when(onlineHearingRepository.findAllByCaseIdIn(caseId)).thenReturn(Arrays.asList(createdOnlineHearing));
        List<OnlineHearing> newOnlineHearing = onlineHearingService.retrieveOnlineHearingByCaseIds(caseId);
        assertEquals(1, newOnlineHearing.size());
    }

    @Test
    public void testRetrieveOnlineHearingByCaseIdsAndState() {
        List<String> caseId = Arrays.asList("foo", "bar");
        Set<String> states = new HashSet<>(Arrays.asList("state"));
        createdOnlineHearing.setOnlineHearingState(onlineHearingState);
        when(onlineHearingRepository.findAllByCaseIdIn(caseId)).thenReturn(Arrays.asList(createdOnlineHearing));
        List<OnlineHearing> newOnlineHearing = onlineHearingService.retrieveOnlineHearingByCaseIds(caseId, Optional.of(states));
        assertEquals(1, newOnlineHearing.size());
    }

    @Test
    public void testRetrieveOnlineHearingByCaseIdsAndDifferentState() {
        List<String> caseId = Arrays.asList("foo", "bar");
        Set<String> states = new HashSet<>(Arrays.asList("foobar"));
        createdOnlineHearing.setOnlineHearingState(onlineHearingState);
        when(onlineHearingRepository.findAllByCaseIdIn(caseId)).thenReturn(Arrays.asList(createdOnlineHearing));
        List<OnlineHearing> newOnlineHearing = onlineHearingService.retrieveOnlineHearingByCaseIds(caseId, Optional.of(states));
        assertEquals(0, newOnlineHearing.size());
    }

    @Test
    public void testDeleteOnlineHearing() {
        UUID uuid = UUID.randomUUID();
        createdOnlineHearing.setOnlineHearingId(uuid);
        createdOnlineHearing.setCaseId("foo");
        doNothing().when(onlineHearingRepository).deleteById(uuid);
        onlineHearingService.deleteOnlineHearing(createdOnlineHearing);
        verify(onlineHearingRepository, times(1)).deleteById(uuid);
    }

    @Test
    public void testDeleteByCaseId() {
        String caseId = "foo";
        createdOnlineHearing.setCaseId(caseId);
        doNothing().when(onlineHearingRepository).deleteByCaseId(caseId);
        onlineHearingService.deleteByCaseId(caseId);
        verify(onlineHearingRepository, times(1)).deleteByCaseId(caseId);
    }
}