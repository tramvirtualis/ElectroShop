package com.hometech.hometech.service;

import com.hometech.hometech.Repository.CartItemRepository;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.Repository.ProductRepository;
import com.hometech.hometech.model.CartItem;
import com.hometech.hometech.model.Cart;
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
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartRepo, ProductRepository productRepo, CustomerRepository customerRepo, UserRepository userRepository) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.userRepository = userRepository;
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

    // 🔹 Xem giỏ hàng của guest theo session
    public List<CartItem> getCartItemsForSession(String sessionId) {
        Customer customer = getOrCreateGuestCustomer(sessionId);
        if (customer.getCart() == null) return List.of();
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
            Cart newCart = new Cart();
            newCart.setCustomer(customer);
            customer.setCart(newCart);
            // Lưu customer để tạo cart (do cascade ALL)
            customerRepo.save(customer);
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

    // 🔹 Thêm sản phẩm vào giỏ cho khách (guest) theo session
    public CartItem addProductForSession(String sessionId, int productId, int quantity) {
        Customer customer = getOrCreateGuestCustomer(sessionId);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<CartItem> existingItems = cartRepo.findByCart(customer.getCart());
        Optional<CartItem> existingItem = existingItems.stream()
                .filter(item -> item.getProduct().getProductID() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartRepo.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setCart(customer.getCart());
            newItem.setQuantity(quantity);
            return cartRepo.save(newItem);
        }
    }

    private Customer getOrCreateGuestCustomer(String sessionId) {
        String guestEmail = "guest_" + sessionId + "@guest.local";
        com.hometech.hometech.model.User user = userRepository.findByEmail(guestEmail);
        if (user == null) {
            user = new com.hometech.hometech.model.User();
            user.setEmail(guestEmail);
            user.setName("Guest");
            user.setActive(true);
            user = userRepository.save(user);
        }

        Customer customer = customerRepo.findByUser_Id(user.getId()).orElse(null);
        if (customer == null) {
            customer = new Customer();
            customer.setUser(user);
            Cart cart = new Cart();
            cart.setCustomer(customer);
            customer.setCart(cart);
            customer = customerRepo.save(customer);
        } else if (customer.getCart() == null) {
            Cart cart = new Cart();
            cart.setCustomer(customer);
            customer.setCart(cart);
            customer = customerRepo.save(customer);
        }
        return customer;
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

    // ===== Guest session operations =====
    public CartItem increaseQuantityForSession(String sessionId, int itemId) {
        Customer guest = getOrCreateGuestCustomer(sessionId);
        CartItem item = cartRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (item.getCart().getCartId() != guest.getCart().getCartId()) {
            throw new RuntimeException("Unauthorized: Cart item does not belong to this session");
        }
        item.setQuantity(item.getQuantity() + 1);
        return cartRepo.save(item);
    }

    public CartItem decreaseQuantityForSession(String sessionId, int itemId) {
        Customer guest = getOrCreateGuestCustomer(sessionId);
        CartItem item = cartRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (item.getCart().getCartId() != guest.getCart().getCartId()) {
            throw new RuntimeException("Unauthorized: Cart item does not belong to this session");
        }
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            return cartRepo.save(item);
        } else {
            cartRepo.deleteById(itemId);
            return null;
        }
    }

    public void removeItemForSession(String sessionId, int itemId) {
        Customer guest = getOrCreateGuestCustomer(sessionId);
        CartItem item = cartRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (item.getCart().getCartId() != guest.getCart().getCartId()) {
            throw new RuntimeException("Unauthorized: Cart item does not belong to this session");
        }
        cartRepo.deleteById(itemId);
    }

    // ===== Merge guest cart (by session) into logged-in user's cart =====
    private Customer findGuestCustomerBySession(String sessionId) {
        String guestEmail = "guest_" + sessionId + "@guest.local";
        com.hometech.hometech.model.User user = userRepository.findByEmail(guestEmail);
        if (user == null) return null;
        return customerRepo.findByUser_Id(user.getId()).orElse(null);
    }

    public void mergeSessionCartToUser(String sessionId, Long userId) {
        Customer guest = findGuestCustomerBySession(sessionId);
        if (guest == null || guest.getCart() == null) return;
        List<CartItem> items = cartRepo.findByCart(guest.getCart());
        if (items == null || items.isEmpty()) return;
        for (CartItem ci : items) {
            addProduct(userId, ci.getProduct().getProductID(), ci.getQuantity());
        }
        // clear guest items
        for (CartItem ci : items) {
            cartRepo.deleteById(ci.getCartItemId());
        }
    }
}
