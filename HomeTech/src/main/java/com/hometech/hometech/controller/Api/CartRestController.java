package com.hometech.hometech.controller.Api;

import com.hometech.hometech.model.CartItem;
import com.hometech.hometech.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final CartService service;

    public CartRestController(CartService service) {
        this.service = service;
    }

    // 🟢 Xem toàn bộ giỏ hàng (deprecated - chỉ dành cho admin)
    @GetMapping
    public ResponseEntity<List<CartItem>> getAll() {
        return ResponseEntity.ok(service.getAllItems());
    }

    // 🟢 Xem giỏ hàng của user cụ thể
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartItem>> getCartByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getCartItemsByUserId(userId));
    }

    // 🟢 Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @RequestParam Long userId,
            @RequestParam int productId,
            @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(service.addProduct(userId, productId, quantity));
    }

    // 🟢 Tăng số lượng
    @PutMapping("/increase/{userId}/{id}")
    public ResponseEntity<CartItem> increase(@PathVariable Long userId, @PathVariable int id) {
        return ResponseEntity.ok(service.increaseQuantity(userId, id));
    }

    // 🟢 Giảm số lượng
    @PutMapping("/decrease/{userId}/{id}")
    public ResponseEntity<CartItem> decrease(@PathVariable Long userId, @PathVariable int id) {
        CartItem updated = service.decreaseQuantity(userId, id);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.noContent().build();
    }

    // 🟢 Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/remove/{userId}/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long userId, @PathVariable int id) {
        service.removeItem(userId, id);
        return ResponseEntity.noContent().build();
    }
}
