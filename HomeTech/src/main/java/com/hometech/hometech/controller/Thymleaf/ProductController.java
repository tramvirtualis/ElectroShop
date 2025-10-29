package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.User;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.model.Review;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final AccountReposirory accountRepository;
    private final UserRepository userRepository;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             ReviewService reviewService,
                             AccountReposirory accountRepository,
                             UserRepository userRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // --- 🧩 Thông tin Session người dùng ---
    private void addSessionInfo(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            model.addAttribute("sessionId", session.getId());
            model.addAttribute("username", session.getAttribute("username"));
            model.addAttribute("isAuthenticated", session.getAttribute("isAuthenticated"));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            model.addAttribute("currentUser", auth.getName());
            model.addAttribute("userAuthorities", auth.getAuthorities());
            // Resolve current userId for forms (reviews)
            Long userId = accountRepository.findByUsername(auth.getName())
                    .map((Account acc) -> {
                        User u = userRepository.findByAccount(acc);
                        return u != null ? u.getId() : null;
                    })
                    .orElse(null);
            model.addAttribute("currentUserId", userId);
        }
    }

    // ---------------------------------------------------------------
    // 🔹 PHẦN NGƯỜI DÙNG (AI CŨNG XEM ĐƯỢC)
    // ---------------------------------------------------------------

    // 🟢 Trang xem tất cả sản phẩm
    @GetMapping
    public String viewAllProducts(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("listProducts", productService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Tất cả sản phẩm");
        return "products/index";
    }

    // 🟢 Xem sản phẩm theo danh mục
    @GetMapping("/category/{categoryId}")
    public String viewProductsByCategory(@PathVariable int categoryId,
                                         HttpServletRequest request,
                                         Model model,
                                         RedirectAttributes ra) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/products";
        }
        model.addAttribute("listProducts", productService.getProductsByCategoryId(categoryId));
        model.addAttribute("currentCategory", category);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Danh mục: " + category.getCategoryName());
        return "products/category";
    }

    // 🟢 Xem chi tiết sản phẩm + danh sách review (chỉ review chưa ẩn)
    @GetMapping("/{id}")
    public String viewProductDetail(@PathVariable("id") int id, Model model, HttpServletRequest request) {
        addSessionInfo(request, model);
        Product product = productService.getById(id);

        // ✅ Chỉ lấy các review chưa bị ẩn
        List<Review> reviews = reviewService.findByProductId(id);
        double averageRating = reviewService.calculateAverageRating(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);

        log.info("[ProductDetail] productId={}, reviewsCount={}, averageRating={}", id, (reviews != null ? reviews.size() : 0), averageRating);

        return "products/detail";
    }

    // 🟢 Hiển thị ảnh sản phẩm
    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable int id) {
        Product product = productService.getById(id);
        if (product != null && product.getImage() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(product.getImage());
        }
        return ResponseEntity.notFound().build();
    }

    // ---------------------------------------------------------------
    // 🔹 PHẦN NGƯỜI DÙNG: GỬI HOẶC CẬP NHẬT ĐÁNH GIÁ
    // ---------------------------------------------------------------

    @PostMapping("/review")
    public String submitReview(@RequestParam("productId") int productId,
                               @RequestParam("userId") int userId,
                               @RequestParam("ratingValue") int ratingValue,
                               @RequestParam("comment") String comment,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               RedirectAttributes ra) {

        try {
            reviewService.addOrUpdateReview(productId, userId, ratingValue, comment, imageFile);
            ra.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm!");
            log.info("[SubmitReview] OK productId={}, userId={}, rating={} ", productId, userId, ratingValue);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể gửi đánh giá: " + e.getMessage());
            log.error("[SubmitReview] FAIL productId={}, userId={}, err={}", productId, userId, e.getMessage());
        }

        return "redirect:/products/" + productId;
    }
}
