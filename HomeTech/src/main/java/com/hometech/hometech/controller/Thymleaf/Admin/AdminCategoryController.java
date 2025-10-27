package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.service.CategoryService;
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

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 🔹 Lưu thông tin session để Thymeleaf hiển thị user hiện tại
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

    // 🟢 1️⃣ Trang danh sách danh mục
    @GetMapping("/category")
    public String listCategories(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("title", "Quản lý danh mục");
        return "admin/categories/index"; // ✅ templates/admin/categories/index.html
    }

    // 🟢 2️⃣ Form thêm danh mục
    @GetMapping("/category/new")
    public String showAddForm(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("category", new Category());
        model.addAttribute("title", "Thêm danh mục mới");
        return "admin/categories/add"; // ✅ templates/admin/categories/add.html
    }

    // 🟢 3️⃣ Lưu danh mục mới
    @PostMapping("/category/add")
    public String addCategory(@ModelAttribute("category") Category category,
                              RedirectAttributes ra) {
        try {
            categoryService.save(category);
            ra.addFlashAttribute("success", "Thêm danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi thêm danh mục: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }

    // 🟢 4️⃣ Hiển thị form chỉnh sửa
    @GetMapping("/category/edit/{id}")
    public String showEditForm(@PathVariable("id") int id,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(id);
        if (category == null) {
            ra.addFlashAttribute("error", "Không tìm thấy danh mục!");
            return "redirect:/admin/category";
        }
        model.addAttribute("category", category);
        model.addAttribute("title", "Chỉnh sửa danh mục");
        return "admin/categories/edit"; // ✅ templates/admin/categories/edit.html
    }

    // 🟢 5️⃣ Cập nhật danh mục
    @PostMapping("/category/update/{id}")
    public String updateCategory(@PathVariable("id") int id,
                                 @ModelAttribute("category") Category category,
                                 RedirectAttributes ra) {
        try {
            Category existing = categoryService.getById(id);
            if (existing == null) {
                ra.addFlashAttribute("error", "Không tìm thấy danh mục!");
                return "redirect:/admin/category";
            }

            existing.setCategoryName(category.getCategoryName());
            categoryService.save(existing);
            ra.addFlashAttribute("success", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }

    // 🟢 6️⃣ Xóa danh mục
    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("success", "Xóa danh mục thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }
}
