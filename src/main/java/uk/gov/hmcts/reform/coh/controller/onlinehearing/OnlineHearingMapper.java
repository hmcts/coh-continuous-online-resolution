package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import uk.gov.hmcts.reform.coh.controller.state.StateResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.stream.Collectors;

public class OnlineHearingMapper {

    private OnlineHearingMapper() {}

    public static void map(OnlineHearingResponse response, OnlineHearing onlineHearing) {
        response.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        response.setCaseId(onlineHearing.getCaseId());
        response.setStartDate(CohISO8601DateFormat.format(onlineHearing.getStartDate()));
        if (onlineHearing.getEndDate() != null) {
            response.setEndDate(CohISO8601DateFormat.format(onlineHearing.getEndDate()));
        }
        response.getCurrentState().setName(onlineHearing.getOnlineHearingState().getState());

        if (onlineHearing.getOnlineHearingStateHistories() != null && !onlineHearing.getOnlineHearingStateHistories().isEmpty()){
            response.getCurrentState().setDatetime
                    (CohISO8601DateFormat.format(onlineHearing.getOnlineHearingStateHistories().stream().sorted(
                            (a, b) -> (b.getDateOccurred().compareTo(a.getDateOccurred()))).collect(Collectors.toList()
                    ).get(onlineHearing.getOnlineHearingStateHistories().size()-1).getDateOccurred()));
        }

        if (onlineHearing.getOnlineHearingStateHistories() != null && !onlineHearing.getOnlineHearingStateHistories().isEmpty()) {
            response.setHistories(onlineHearing
                    .getOnlineHearingStateHistories()
                    .stream()
                    .map(h -> {
                        return new StateResponse(h.getOnlinehearingstate().getState(), CohISO8601DateFormat.format(h.getDateOccurred()));
                    })
                    .collect(Collectors.toList()));
        }

        RelistingResponse relistingResponse = new RelistingResponse(
            onlineHearing.getRelistReason(),
            onlineHearing.getRelistState(),
            onlineHearing.getRelistCreated(),
            onlineHearing.getRelistUpdated()
        );

        response.setRelisting(relistingResponse);

        response.setRelistingHistory(
            onlineHearing.getRelistingHistories().stream()
                .map(entry
                    -> new RelistingHistoryResponse(
                    entry.getRelistReason(),
                    entry.getRelistState(),
                    entry.getDateOccurrred()
                ))
                .collect(Collectors.toList())
        );
    }
}
