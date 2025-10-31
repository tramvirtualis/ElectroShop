package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.service.OrderService;
import com.hometech.hometech.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminCategoryController(CategoryService categoryService,
                                   ProductService productService,
                                   OrderService orderService,
                                   UserService userService) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    /** 🧠 Gắn thông tin session để Thymeleaf hiển thị */
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

    /** 🟢 Hiển thị tất cả danh mục trong dashboard */
    @GetMapping("/dashboard/category")
    public String listCategories(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("products", productService.getAll());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("totalUsers", userService.countAll());
        model.addAttribute("activeUsers", userService.countByStatus(true));
        model.addAttribute("inactiveUsers", userService.countByStatus(false));
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        model.addAttribute("totalProducts", productService.getAll().size());
        model.addAttribute("totalCategories", categoryService.getAll().size());
        model.addAttribute("dashboardSection", "categories");
        model.addAttribute("title", "Quản lý danh mục");
        return "admin/dashboard"; // ✅ luôn load dashboard.html
    }

    /** 🟢 Hiển thị form thêm danh mục */
    @GetMapping("/dashboard/category/new")
    public String showAddForm(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("category", new Category());
        model.addAttribute("dashboardSection", "categories");
        model.addAttribute("showCategoryForm", true);
        model.addAttribute("title", "Thêm danh mục mới");

        // load thêm dữ liệu cho dashboard
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("products", productService.getAll());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/dashboard";
    }

    /** 🟢 Lưu danh mục mới */
    @PostMapping("/dashboard/category/add")
    public String addCategory(@RequestParam("categoryName") String categoryName,
                              RedirectAttributes ra) {
        try {
            Category category = new Category();
            category.setCategoryID(0); // Explicitly set to 0 to trigger ID generation
            category.setCategoryName(categoryName);
            categoryService.save(category);
            ra.addFlashAttribute("success", "Thêm danh mục thành công với ID: " + category.getCategoryID() + "!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi thêm danh mục: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    /** 🟢 Hiển thị form chỉnh sửa */
    @GetMapping("/dashboard/category/edit/{id}")
    public String showEditForm(@PathVariable("id") int id,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(id);
        if (category == null) {
            ra.addFlashAttribute("error", "Không tìm thấy danh mục!");
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("category", category);
        model.addAttribute("dashboardSection", "categories");
        model.addAttribute("showCategoryForm", true);
        model.addAttribute("title", "Chỉnh sửa danh mục");
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("products", productService.getAll());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/dashboard";
    }

    /** 🟢 Cập nhật danh mục */
    @PostMapping("/dashboard/category/update/{id}")
    public String updateCategory(@PathVariable("id") int id,
                                 @RequestParam("categoryName") String categoryName,
                                 RedirectAttributes ra) {
        try {
            Category existing = categoryService.getById(id);
            if (existing == null) {
                ra.addFlashAttribute("error", "Không tìm thấy danh mục!");
                return "redirect:/admin/dashboard";
            }

            existing.setCategoryName(categoryName);
            categoryService.save(existing);
            ra.addFlashAttribute("success", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    /** 🟢 Xóa danh mục */
    @GetMapping("/dashboard/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("success", "Xóa danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
