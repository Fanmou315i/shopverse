package com.shopverse.app.api;
import com.shopverse.app.domain.ChatMessage;
import com.shopverse.app.repo.ChatMessageRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
 private final ChatMessageRepository messages;
 private static final Map<String,String> NAMES=Map.of("service","平台客服","digital","声域数码","outdoor","原野运动","daily","日常生活研究所");
 public ConversationController(ChatMessageRepository messages){this.messages=messages;}
 @GetMapping public Object list(){return NAMES.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->{var list=thread(e.getKey());return Map.of("id",e.getKey(),"name",e.getValue(),"lastMessage",list.isEmpty()?"点击开始会话":list.get(list.size()-1).getContent(),"lastAt",list.isEmpty()?"":list.get(list.size()-1).getSentAt().toString());}).toList();}
 @GetMapping("/{id}/messages") public List<ChatMessage> thread(@PathVariable String id){check(id);return messages.findAllByOrderBySentAtAsc().stream().filter(m->id.equals(m.getConversationId())||(id.equals("service")&&m.getConversationId()==null)).toList();}
 @PostMapping("/{id}/messages") @Transactional public ChatMessage send(@PathVariable String id,@RequestBody Body body){check(id);if(body.content()==null||body.content().isBlank()||body.content().length()>1000)throw new IllegalArgumentException("消息需为1到1000字");var m=new ChatMessage("我",body.content().trim());m.setConversationId(id);messages.save(m);var reply=new ChatMessage(NAMES.get(id),"【自动演示回复】已收到您的消息。此会话用于展示消息收发与会话隔离，尚未连接真人客服。");reply.setConversationId(id);messages.save(reply);return m;}
 private void check(String id){if(!NAMES.containsKey(id))throw new IllegalArgumentException("会话不存在");}
 public record Body(String content){}
}
