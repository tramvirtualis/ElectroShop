package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.model.Category;
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

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;


    public ProductController(ProductService productService, CategoryService categoryService, ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
    }

    // --- Thông tin Session người dùng ---
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
        return "products/index"; // ✅ templates/products/index.html
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
        return "products/category"; // ✅ templates/products/category.html
    }

    // 🟢 Xem chi tiết sản phẩm
    @GetMapping("/{id}")
    public String viewProductDetail(@PathVariable("id") int id, Model model) {
        Product product = productService.getById(id);
        List<Review> reviews = reviewService.getReviewsByProduct(id);
        double averageRating = reviewService.getAverageRating(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);

        return "products/detail"; // trỏ tới templates/products/detail.html
    }

    // 🟢 Endpoint hiển thị ảnh sản phẩm
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

}
