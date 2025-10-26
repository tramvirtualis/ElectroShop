package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
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

    // 🟢 Hiển thị tất cả sản phẩm
    @GetMapping
    public String viewHomePage(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("listProducts", productService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Tất cả sản phẩm");
        return "products/index";
    }

    // 🟢 Hiển thị sản phẩm theo danh mục
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

    // 🟢 Hiển thị sản phẩm đang hoạt động theo danh mục
    @GetMapping("/category/{categoryId}/active")
    public String viewActiveProductsByCategory(@PathVariable int categoryId,
                                               HttpServletRequest request,
                                               Model model,
                                               RedirectAttributes ra) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/products";
        }
        model.addAttribute("listProducts", productService.getActiveProductsByCategoryId(categoryId));
        model.addAttribute("currentCategory", category);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Sản phẩm đang hoạt động - " + category.getCategoryName());
        return "products/category";
    }

    // 🟢 Hiển thị form thêm sản phẩm mới
    @GetMapping("/new")
    public String showAddForm(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Thêm sản phẩm mới");
        return "products/add";
    }

    // 🟢 Lưu sản phẩm mới
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              HttpServletRequest request,
                              Model model,
                              RedirectAttributes ra) {
        addSessionInfo(request, model);
        productService.save(product);
        ra.addFlashAttribute("success", "Thêm sản phẩm thành công!");
        return "redirect:/products";
    }

    // 🟢 Hiển thị chi tiết sản phẩm
    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable int id,
                                    HttpServletRequest request,
                                    Model model,
                                    RedirectAttributes ra) {
        addSessionInfo(request, model);
        Product product = productService.getById(id);
        if (product == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Chi tiết sản phẩm - " + product.getProductName());
        return "products/detail";
    }

    // 🟢 Hiển thị form sửa sản phẩm
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        addSessionInfo(request, model);
        Product product = productService.getById(id);
        if (product == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm cần sửa!");
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Chỉnh sửa sản phẩm");
        return "products/edit";
    }

    // 🟢 Cập nhật sản phẩm
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable int id,
                                @ModelAttribute("product") Product product,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes ra) {
        addSessionInfo(request, model);
        product.setProductID(id);
        productService.save(product);
        ra.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        return "redirect:/products";
    }

    // 🟢 Xóa sản phẩm
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id,
                                HttpServletRequest request,
                                Model model,
                                RedirectAttributes ra) {
        addSessionInfo(request, model);
        productService.delete(id);
        ra.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        return "redirect:/products";
    }
}
