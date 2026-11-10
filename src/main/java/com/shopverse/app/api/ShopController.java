package com.shopverse.app.api;

import com.shopverse.app.domain.CartItem;
import com.shopverse.app.domain.ChatMessage;
import com.shopverse.app.domain.OrderRecord;
import com.shopverse.app.domain.Product;
import com.shopverse.app.repo.OrderRepository;
import com.shopverse.app.service.CartService;
import com.shopverse.app.service.CatalogService;
import com.shopverse.app.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShopController {
    private final CatalogService catalog;
    private final CartService cart;
    private final MessageService messages;
    private final OrderRepository orders;

    public ShopController(CatalogService catalog, CartService cart, MessageService messages, OrderRepository orders) {
        this.catalog = catalog; this.cart = cart; this.messages = messages; this.orders = orders;
    }

    @GetMapping("/home")
    public Map<String, Object> home() {
        return Map.of(
            "categories", List.of(
                Map.of("name", "数码", "icon", "⌁"), Map.of("name", "运动", "icon", "◒"),
                Map.of("name", "家居", "icon", "⌂"), Map.of("name", "生活", "icon", "✦"),
                Map.of("name", "美妆", "icon", "◈"), Map.of("name", "手工", "icon", "◍")
            ),
            "campaign", Map.of("title", "品质好物 · 限时精选", "subtitle", "精选商品满 ¥299 减 ¥40", "code", "SAVE40"),
            "recommendations", catalog.list("", "")
        );
    }

    @GetMapping("/products")
    public List<Product> products(@RequestParam(required = false) String query, @RequestParam(required = false) String category) {
        return catalog.list(query, category);
    }

    @PostMapping("/cart/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addToCart(@RequestBody ProductIdRequest request) { return cart.add(request.productId(), request.variant()); }

    @GetMapping("/cart")
    public Map<String, Object> getCart() { return cartResponse(); }

    @PutMapping("/cart/items/{id}")
    public CartItem updateCart(@PathVariable Long id, @RequestBody CartUpdateRequest request) {
        return cart.update(id, request.quantity(), request.selected());
    }

    @DeleteMapping("/cart/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCartItem(@PathVariable Long id) { cart.remove(id); }

    @PutMapping("/cart/selection")
    public Map<String, Object> selectAll(@RequestBody SelectionRequest request) {
        cart.selectAll(request.selected());
        return cartResponse();
    }

    @PostMapping("/cart/checkout")
    public OrderRecord checkout(@RequestBody(required = false) CheckoutRequest request) {
        return cart.checkout(meta(request));
    }

    @PostMapping("/orders/direct")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderRecord directCheckout(@RequestBody DirectCheckoutRequest request) {
        if (request.productId() == null) throw new IllegalArgumentException("请选择商品");
        return cart.checkoutDirect(request.productId(), request.variant(), request.quantity(), meta(request));
    }

    @GetMapping("/messages")
    public List<ChatMessage> getMessages() { return messages.list(); }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessage sendMessage(@RequestBody MessageRequest request) { return messages.send(request.content()); }

    @GetMapping("/profile")
    public Map<String, Object> profile() {
        return Map.of(
            "user", Map.of("name", "陈小满", "level", "SV 银卡会员", "points", 860, "avatar", "陈"),
            "wallet", Map.of("coupons", 3, "favorites", 12, "footprints", 8),
            "addresses", List.of(Map.of("name", "陈小满", "phone", "138****2026", "address", "上海市 浦东新区 春晓路 88 号")),
            "orders", orders.findTop10ByOrderByCreatedAtDesc()
        );
    }

    private Map<String, Object> cartResponse() {
        List<CartItem> items = cart.list();
        BigDecimal total = items.stream().filter(CartItem::isSelected).map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int selectedCount = items.stream().filter(CartItem::isSelected).mapToInt(CartItem::getQuantity).sum();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items); body.put("selectedCount", selectedCount); body.put("total", total); body.put("allSelected", !items.isEmpty() && items.stream().allMatch(CartItem::isSelected));
        return body;
    }

    public record ProductIdRequest(Long productId, String variant) { }
    public record CartUpdateRequest(int quantity, boolean selected) { }
    public record SelectionRequest(boolean selected) { }
    public record MessageRequest(String content) { }
    private CartService.PurchaseMeta meta(CheckoutRequest request) {
        return new CartService.PurchaseMeta(request == null ? "" : request.couponCode(), request == null ? "" : request.paymentMethod(), request == null ? "" : request.receiverName(), request == null ? "" : request.receiverPhone(), request == null ? "" : request.receiverAddress());
    }
    private CartService.PurchaseMeta meta(DirectCheckoutRequest request) {
        return new CartService.PurchaseMeta(request == null ? "" : request.couponCode(), request == null ? "" : request.paymentMethod(), request == null ? "" : request.receiverName(), request == null ? "" : request.receiverPhone(), request == null ? "" : request.receiverAddress());
    }
    public record CheckoutRequest(String couponCode, String paymentMethod, String receiverName, String receiverPhone, String receiverAddress) { }
    public record DirectCheckoutRequest(Long productId, String variant, int quantity, String couponCode, String paymentMethod, String receiverName, String receiverPhone, String receiverAddress) { }
}
