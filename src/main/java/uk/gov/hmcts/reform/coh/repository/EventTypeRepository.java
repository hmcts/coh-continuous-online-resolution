package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.EventType;

@Repository
public interface EventTypeRepository extends CrudRepository<EventType, Integer> {
}
