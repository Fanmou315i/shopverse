package com.shopverse.app.repo;
import com.shopverse.app.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByOrderBySentAtAsc();
}
