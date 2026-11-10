package com.shopverse.app.service;

import com.shopverse.app.domain.ChatMessage;
import com.shopverse.app.events.CommerceEventGateway;
import com.shopverse.app.repo.ChatMessageRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {
    private final ChatMessageRepository messages;
    private final CommerceEventGateway events;
    public MessageService(ChatMessageRepository messages, CommerceEventGateway events) { this.messages = messages; this.events = events; }
    public List<ChatMessage> list() { return messages.findAllByOrderBySentAtAsc(); }
    public ChatMessage send(String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("消息内容不能为空");
        ChatMessage message = messages.save(new ChatMessage("我", content.trim()));
        events.publish("MESSAGE_SENT", "{\"messageId\":" + message.getId() + "}");
        return message;
    }
}
