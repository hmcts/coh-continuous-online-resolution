package uk.gov.hmcts.reform.coh.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class EventForwardingRegisterService {

    public boolean registerForEvent(String jurisdiction, String eventType) {
        return true;
    }
}
