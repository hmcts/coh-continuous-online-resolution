package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequestMapper;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.repository.DecisionRepository;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DecisionServiceTest {

    @InjectMocks
    private ExpiryCalendar expiryCalendar;

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private DecisionStateService decisionStateService;

    private DecisionService decisionService;

    private Decision decision;

    private Decision newDecision;

    private DecisionState decisionState;

    private UUID uuid;

    @Before
    public void setup() throws IOException {
        expiryCalendar.init();
        uuid = UUID.randomUUID();

        DecisionRequest request = (DecisionRequest) JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);

        decisionState = new DecisionState();
        decisionState.setState("decision_drafted");
        decisionService = new DecisionService(decisionRepository);

        decision = new Decision();
        DecisionRequestMapper.map(request, decision, decisionState);

        newDecision = new Decision();
        DecisionRequestMapper.map(request, newDecision, decisionState);
        newDecision.setDecisionId(UUID.randomUUID());

        given(decisionStateService.retrieveDecisionStateByState("decision_drafted")).willReturn(Optional.ofNullable(decisionState));
    }

    @Test
    public void testCreateDecision() {
        when(decisionRepository.save(decision)).thenReturn(newDecision);
        assertEquals(newDecision, decisionService.createDecision(decision));
    }

    @Test
    public void testFindByOnlineHearingId() {
        when(decisionRepository.findByOnlineHearingOnlineHearingId(uuid)).thenReturn(Optional.ofNullable(decision));
        assertEquals(decision, decisionService.findByOnlineHearingId(uuid).get());
    }

    @Test
    public void testFindByOnlineHearingIdFail() {
        when(decisionRepository.findByOnlineHearingOnlineHearingId(uuid)).thenReturn(Optional.empty());
        assertFalse(decisionService.findByOnlineHearingId(UUID.randomUUID()).isPresent());
    }

    @Test
    public void testRetrieveByOnlineHearingIdAndDecisionId() {
        when(decisionRepository.findByOnlineHearingOnlineHearingIdAndDecisionId(uuid, uuid)).thenReturn(Optional.empty());
        assertFalse(decisionService.retrieveByOnlineHearingIdAndDecisionId(uuid, uuid).isPresent());
    }

    @Test
    public void testUpdateDecision() {
        when(decisionRepository.save(decision)).thenReturn(decision);
        assertEquals(decision, decisionService.updateDecision(decision));
    }

    @Test
    public void testDeleteDecisionById() {
        doNothing().when(decisionRepository).deleteById(uuid);
        decisionService.deleteDecisionById(uuid);
    }

    @Test
    public void testDeadlineExpiryDate() {
        DateFormat df = new SimpleDateFormat("yyyyMMDDHHmmss");
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.DAY_OF_YEAR, expiryCalendar.getDeadlineExtensionDays());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        Date expiryDate = expiryCalendar.getDeadlineExpiryDate();

        assertTrue(df.format(calendar.getTime()).equalsIgnoreCase(df.format(expiryDate)));
    }
}