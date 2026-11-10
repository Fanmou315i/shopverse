package com.shopverse.app.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="consumed_events")
public class ConsumedEvent {
    @Id @Column(length=200) public String eventId;
    public LocalDateTime consumedAt;
    protected ConsumedEvent() {}
    public ConsumedEvent(String id) { eventId=id; consumedAt=LocalDateTime.now(); }
}
