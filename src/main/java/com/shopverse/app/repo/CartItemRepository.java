package com.shopverse.app.repo;
import com.shopverse.app.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByProductId(Long productId);
    Optional<CartItem> findByProductIdAndVariant(Long productId, String variant);
}
