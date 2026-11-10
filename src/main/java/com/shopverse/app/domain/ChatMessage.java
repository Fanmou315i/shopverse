package com.shopverse.app.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    private String conversationId;
    public String getConversationId() { return conversationId; }
    public void setConversationId(String value) { conversationId = value; }
    @Column(length = 1000) private String content;
    private LocalDateTime sentAt;

    protected ChatMessage() { }
    public ChatMessage(String sender, String content) {
        this.sender = sender; this.content = content; this.sentAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
}
