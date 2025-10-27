package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.model.Review;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
@RequestMapping("/admin")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;


    public AdminProductController(ProductService productService, CategoryService categoryService, ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
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
    // 🟢 Trang quản trị danh sách sản phẩm
    @GetMapping("/product")
    public String adminProductList(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("listProducts", productService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Quản lý sản phẩm");
        return "admin/products/index"; // ✅ templates/admin/products/index.html
    }

    // 🟢 Hiển thị form thêm sản phẩm
    @GetMapping("/product/new")
    public String showAddForm(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Thêm sản phẩm mới");
        return "admin/products/add"; // ✅ templates/admin/products/add.html
    }

    // 🟢 Lưu sản phẩm mới
    @PostMapping("/product/add")
    public String addProduct(@RequestParam("name") String name,
                             @RequestParam("price") Double price,
                             @RequestParam("description") String description,
                             @RequestParam(value = "categoryID", required = false) Integer categoryId,
                             @RequestParam("image") MultipartFile imageFile,
                             RedirectAttributes ra) {

        try {
            Product product = new Product();
            product.setProductName(name);
            product.setPrice(price);
            product.setDescription(description);
            if (categoryId != null) {
                product.setCategory(categoryService.getById(categoryId));
            }
            if (!imageFile.isEmpty()) {
                product.setImage(imageFile.getBytes());
            }
            productService.save(product);
            ra.addFlashAttribute("success", "Thêm sản phẩm thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi tải ảnh lên!");
        }
        return "redirect:/admin/product";
    }

    // 🟢 Form chỉnh sửa sản phẩm
    @GetMapping("/product/edit/{id}")
    public String showEditForm(@PathVariable int id,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        addSessionInfo(request, model);
        Product product = productService.getById(id);
        if (product == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm cần sửa!");
            return "redirect:/admin/product";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Chỉnh sửa sản phẩm");
        return "admin/products/edit"; // ✅ templates/admin/products/edit.html
    }

    // 🟢 Cập nhật sản phẩm (có thể đổi ảnh)
    @PostMapping("/product/update/{id}")
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
                return "redirect:/admin/product";
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
        return "redirect:/admin/product";
    }

    // 🟢 Xóa sản phẩm
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable int id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        return "redirect:/admin/product";
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
            return "redirect:/admin/product";
        }
        model.addAttribute("listProducts", productService.getProductsByCategoryId(categoryId));
        model.addAttribute("currentCategory", category);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Danh mục: " + category.getCategoryName());
        return "admin/category/product"; // ✅ templates/products/category.html
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

        return "/admin/products/detail"; // trỏ tới templates/products/detail.html
    }
    @GetMapping("/product/image/{id}")
    @ResponseBody
    public byte[] getProductImage(@PathVariable int id) {
        Product product = productService.getById(id);
        return (product != null && product.getImage() != null) ? product.getImage() : new byte[0];
    }

}
