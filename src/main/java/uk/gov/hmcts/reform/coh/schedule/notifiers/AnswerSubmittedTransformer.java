package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;

import java.util.Arrays;
import java.util.List;

@Component
public class AnswerSubmittedTransformer implements EventTransformer<OnlineHearing> {

    @Override
    public NotificationRequest transform(SessionEventType sessionEventType, OnlineHearing onlineHearing) {

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseId(onlineHearing.getCaseId());
        notificationRequest.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        notificationRequest.setEventType(sessionEventType.getEventTypeName());

        return notificationRequest;
    }

    @Override
    public List<String> supports() {
        return Arrays.asList(EventTypes.ANSWERS_SUBMITTED.getEventType());
    }
}
