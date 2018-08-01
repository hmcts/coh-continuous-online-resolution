package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionEventRepository extends CrudRepository<SessionEvent, UUID> {

    List<SessionEvent> findAllByOnlineHearing(OnlineHearing onlineHearing);

    List<SessionEvent> findAllBySessionEventForwardingState(SessionEventForwardingState sessionEventForwardingState);

    void deleteByOnlineHearing(OnlineHearing onlineHearing);

    List<SessionEvent> findAllBySessionEventForwardingRegister(Optional<SessionEventForwardingRegister> sessionEventForwardingRegister);
}
