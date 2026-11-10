package com.shopverse.app.events;

import com.shopverse.app.repo.OutboxEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "commerce.kafka.enabled", havingValue = "true")
public class OutboxKafkaPublisher {
    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    public OutboxKafkaPublisher(OutboxEventRepository outbox, KafkaTemplate<String, String> kafka) { this.outbox = outbox; this.kafka = kafka; }
    @Scheduled(fixedDelayString = "${commerce.outbox.poll-interval-ms:250}")
    @Transactional
    public void publishBatch() {
        for (var event : outbox.lockReady(LocalDateTime.now(), PageRequest.of(0, 100))) {
            try {
                kafka.send("commerce.events", event.getEventType(), event.getPayload()).get(3, TimeUnit.SECONDS);
                event.markPublished();
            } catch (Exception ignored) { event.retryLater(); }
        }
    }
}
