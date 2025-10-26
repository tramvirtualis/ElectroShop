package com.hometech.hometech.service;

import com.hometech.hometech.Repository.CartItemRepository;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.Repository.ProductRepository;
import com.hometech.hometech.model.CartItem;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;

    public CartService(CartItemRepository cartRepo, ProductRepository productRepo, CustomerRepository customerRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
    }

    // 🔹 Xem toàn bộ giỏ hàng (deprecated - chỉ dành cho admin)
    public List<CartItem> getAllItems() {
        return cartRepo.findAll();
    }

    // 🔹 Xem giỏ hàng của user cụ thể
    public List<CartItem> getCartItemsByUserId(Long userId) {
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (customer.getCart() == null) {
            return List.of(); // Trả về danh sách rỗng nếu chưa có cart
        }
        
        return cartRepo.findByCart(customer.getCart());
    }

    // 🔹 Thêm sản phẩm vào giỏ
    public CartItem addProduct(Long userId, int productId, int quantity) {
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Đảm bảo customer có cart
        if (customer.getCart() == null) {
            throw new RuntimeException("Customer cart not found");
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ của user này chưa
        List<CartItem> existingItems = cartRepo.findByCart(customer.getCart());
        Optional<CartItem> existingItem = existingItems.stream()
                .filter(item -> item.getProduct().getProductID() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            // Nếu sản phẩm đã có trong giỏ -> tăng số lượng
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartRepo.save(item);
        } else {
            // Tạo cart item mới
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setCart(customer.getCart());
            newItem.setQuantity(quantity);
            return cartRepo.save(newItem);
        }
    }

    // 🔹 Tăng số lượng sản phẩm
    public CartItem increaseQuantity(Long userId, int itemId) {
        CartItem item = cartRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Kiểm tra xem item có thuộc về user này không
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (item.getCart().getCartId() != customer.getCart().getCartId()) {
            throw new RuntimeException("Unauthorized: Cart item does not belong to this user");
        }
        
        item.setQuantity(item.getQuantity() + 1);
        return cartRepo.save(item);
    }

    // 🔹 Giảm số lượng sản phẩm
    public CartItem decreaseQuantity(Long userId, int itemId) {
        CartItem item = cartRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Kiểm tra xem item có thuộc về user này không
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (item.getCart().getCartId() != customer.getCart().getCartId()) {
            throw new RuntimeException("Unauthorized: Cart item does not belong to this user");
        }
        
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            return cartRepo.save(item);
        } else {
            // Nếu còn 1 thì xóa luôn
            cartRepo.deleteById(itemId);
            return null;
        }
    }

    // 🔹 Xóa sản phẩm khỏi giỏ
    public void removeItem(Long userId, int itemId) {
        CartItem item = cartRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Kiểm tra xem item có thuộc về user này không
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (item.getCart().getCartId() != customer.getCart().getCartId()) {
            throw new RuntimeException("Unauthorized: Cart item does not belong to this user");
        }
        
        cartRepo.deleteById(itemId);
    }
}
