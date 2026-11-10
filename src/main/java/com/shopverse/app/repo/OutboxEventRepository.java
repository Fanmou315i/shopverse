package com.shopverse.app.repo;

import com.shopverse.app.domain.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEvent e where e.status = 'PENDING' and e.nextAttemptAt <= :now order by e.id")
    List<OutboxEvent> lockReady(@Param("now") LocalDateTime now, Pageable pageable);
}
