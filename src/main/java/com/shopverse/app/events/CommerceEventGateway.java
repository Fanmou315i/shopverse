package com.shopverse.app.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.shopverse.app.domain.OutboxEvent;
import com.shopverse.app.repo.OutboxEventRepository;
import java.util.UUID;

@Component
public class CommerceEventGateway {
    private static final Logger log = LoggerFactory.getLogger(CommerceEventGateway.class);
    private final OutboxEventRepository outbox;
    public CommerceEventGateway(OutboxEventRepository outbox) { this.outbox = outbox; }

    public void publish(String type, String payload) {
        String id = UUID.randomUUID().toString();
        String event = "{\"eventId\":\"" + id + "\",\"type\":\"" + type + "\",\"payload\":" + payload + "}";
        outbox.save(new OutboxEvent(id, type, event));
        log.debug("Outbox event persisted: {}", type);
    }
}
