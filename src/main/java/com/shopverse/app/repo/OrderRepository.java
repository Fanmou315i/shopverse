package com.shopverse.app.repo;
import com.shopverse.app.domain.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<OrderRecord, Long> {
    List<OrderRecord> findTop10ByOrderByCreatedAtDesc();
}
