package com.shopverse.app.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopverse.app.domain.*;
import jakarta.persistence.EntityManager;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name="commerce.kafka.enabled", havingValue="true")
public class OrderEventConsumer {
    private final EntityManager em;
    private final ObjectMapper mapper;
    public OrderEventConsumer(EntityManager em, ObjectMapper mapper) { this.em=em; this.mapper=mapper; }
    @KafkaListener(topics="commerce.events", groupId="shopverse-order-notifications-v1")
    @Transactional
    public void consume(ConsumerRecord<String,String> record) throws Exception {
        var event=mapper.readTree(record.value());
        if (!"ORDER_CREATED".equals(event.path("type").asText())) return;
        String orderNo=event.path("payload").path("orderNo").asText();
        if (orderNo.isBlank()) throw new IllegalArgumentException("Missing orderNo");
        // Legacy events have no eventId; stable order number permits safe historical replay.
        String id=event.path("eventId").asText("ORDER_CREATED:"+orderNo);
        if (em.find(ConsumedEvent.class,id)!=null) return;
        em.persist(new ConsumedEvent(id));
        em.flush(); // Unique key arbitrates concurrent duplicate delivery.
        ChatMessage message=new ChatMessage("系统通知", "订单 " + orderNo + " 已创建成功，商家将尽快安排发货。");
        message.setConversationId("system");
        em.persist(message);
    }
}
