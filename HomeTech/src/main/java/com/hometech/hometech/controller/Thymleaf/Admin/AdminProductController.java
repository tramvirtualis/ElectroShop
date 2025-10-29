package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.model.Review;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.service.ReviewService;
import com.hometech.hometech.service.OrderService;
import com.hometech.hometech.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final OrderService orderService;
    private final UserService userService;


    public AdminProductController(ProductService productService, CategoryService categoryService, ReviewService reviewService,
                                  OrderService orderService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.orderService = orderService;
        this.userService = userService;
    }
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
    // (bỏ endpoint JSON trùng /admin để tránh xung đột với các route view)

    // 🟢 Hiển thị form thêm sản phẩm
    @GetMapping("/dashboard/product/new")
    public String showAddForm(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        var products = productService.getAll();
        long activeProductsCount = products.stream().filter(p -> p.isStatus()).count();
        long inactiveProductsCount = products.size() - activeProductsCount;
        model.addAttribute("products", products);
        model.addAttribute("activeProductsCount", activeProductsCount);
        model.addAttribute("inactiveProductsCount", inactiveProductsCount);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("product", new Product());
        model.addAttribute("dashboardSection", "products");
        model.addAttribute("showProductForm", true);
        model.addAttribute("title", "Thêm sản phẩm mới");
        return "admin/dashboard";
    }

    // 🟢 Lưu sản phẩm mới


    @PostMapping(value = "/dashboard/product/save", consumes = "multipart/form-data")
    public String saveProduct(@RequestParam(value = "productID", required = false) Integer productId,
                              @RequestParam("productName") String name,
                              @RequestParam("price") Double price,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "categoryID", required = false) Integer categoryId,
                              @RequestParam(value = "status", required = false) Boolean status,
                              @RequestParam(value = "image", required = false) MultipartFile imageFile,
                              RedirectAttributes ra) {

        try {
            System.out.println("🟡 File nhận được: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));
            System.out.println("🟡 Dung lượng file: " + (imageFile != null ? imageFile.getSize() : 0));

            Product product;

            if (productId != null && productId > 0) {
                product = productService.getById(productId);
                if (product == null) {
                    ra.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
                    return "redirect:/admin/dashboard";
                }
            } else {
                product = new Product();
            }

            product.setProductName(name);
            product.setPrice(price != null ? price : 0.0);
            product.setDescription(description);
            product.setStatus(status != null && status);

            if (categoryId != null) {
                product.setCategory(categoryService.getById(categoryId));
            }

            // 🟢 Xử lý ảnh
            if (imageFile != null && !imageFile.isEmpty()) {
                byte[] imageBytes = imageFile.getBytes();
                System.out.println("🟢 Ảnh upload có dung lượng: " + imageBytes.length);
                product.setImage(imageBytes);
            }

            productService.save(product);
            ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }




    // 🟢 Form chỉnh sửa sản phẩm
    @GetMapping("/dashboard/product/edit/{id}")
    public String showEditForm(@PathVariable int id,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        addSessionInfo(request, model);
        Product product = productService.getById(id);
        if (product == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm cần sửa!");
            return "redirect:/admin/dashboard";
        }
        var products = productService.getAll();
        long activeProductsCount = products.stream().filter(p -> p.isStatus()).count();
        long inactiveProductsCount = products.size() - activeProductsCount;
        model.addAttribute("products", products);
        model.addAttribute("activeProductsCount", activeProductsCount);
        model.addAttribute("inactiveProductsCount", inactiveProductsCount);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("dashboardSection", "products");
        model.addAttribute("showProductForm", true);
        model.addAttribute("title", "Chỉnh sửa sản phẩm");
        return "admin/dashboard";
    }

    // 🟢 Cập nhật sản phẩm (có thể đổi ảnh)
    @PostMapping("/dashboard/product/update/{id}")
    public String updateProduct(@PathVariable int id,
                                @RequestParam("name") String name,
                                @RequestParam("price") Double price,
                                @RequestParam("description") String description,
                                @RequestParam(value = "categoryID", required = false) Integer categoryId,
                                @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                RedirectAttributes ra) {
        try {
            Product existing = productService.getById(id);
            if (existing == null) {
                ra.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
                return "redirect:/admin/dashboard";
            }

            existing.setProductName(name);
            existing.setPrice(price);
            existing.setDescription(description);
            if (categoryId != null) {
                existing.setCategory(categoryService.getById(categoryId));
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                existing.setImage(imageFile.getBytes());
            }

            productService.save(existing);
            ra.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi cập nhật ảnh!");
        }
        return "redirect:/admin/dashboard";
    }

    // 🟢 Xóa sản phẩm
    @GetMapping("/dashboard/product/delete/{id}")
    public String deleteProduct(@PathVariable int id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        return "redirect:/admin/dashboard";
    }


    // 🟢 Xem sản phẩm theo danh mục
    @GetMapping("/dashboard/category/{categoryId}")
    public String viewProductsByCategory(@PathVariable int categoryId,
                                         HttpServletRequest request,
                                         Model model,
                                         RedirectAttributes ra) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("listProducts", productService.getProductsByCategoryId(categoryId));
        model.addAttribute("currentCategory", category);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Danh mục: " + category.getCategoryName());
        return "admin/category/product";
    }

    // 🟢 Xem chi tiết sản phẩm
    @GetMapping("/dashboard/product/{id}")
    public String viewProductDetail(@PathVariable("id") int id, Model model) {
        Product product = productService.getById(id);
        List<Review> reviews = reviewService.getAllReviewsByProduct(id);
        double averageRating = reviewService.getAverageRating(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);

        return "/admin/products/detail";
    }
    @GetMapping("/dashboard/product/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        Product product = productService.getById(id);
        if (product == null || product.getImage() == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(product.getImage());
    }


}
