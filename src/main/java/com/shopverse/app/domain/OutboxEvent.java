package com.shopverse.app.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events", indexes = {@Index(name = "idx_outbox_ready", columnList = "status,nextAttemptAt"), @Index(name = "idx_outbox_type", columnList = "eventType,createdAt")})
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 36) private String eventId;
    @Column(nullable = false, length = 60) private String eventType;
    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT") private String payload;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false) private int attempts;
    @Column(nullable = false) private LocalDateTime nextAttemptAt;
    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    protected OutboxEvent() { }
    public OutboxEvent(String eventId, String eventType, String payload) {
        this.eventId = eventId; this.eventType = eventType; this.payload = payload; this.status = "PENDING";
        this.attempts = 0; this.createdAt = LocalDateTime.now(); this.nextAttemptAt = createdAt;
    }
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public boolean isReady() { return "PENDING".equals(status) && !nextAttemptAt.isAfter(LocalDateTime.now()); }
    public void markPublished() { status = "PUBLISHED"; publishedAt = LocalDateTime.now(); }
    public void retryLater() { attempts++; nextAttemptAt = LocalDateTime.now().plusSeconds(Math.min(300, 1L << Math.min(attempts, 8))); }
}
