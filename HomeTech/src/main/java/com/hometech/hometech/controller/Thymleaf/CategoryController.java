package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
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

    @GetMapping
    public String viewAll(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("categories", categoryService.getAll());
        return "categories/index";
    }

    @GetMapping("/{id}")
    public String viewCategoryDetail(@PathVariable int id, HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(id);
        if (category == null) return "redirect:/categories";
        model.addAttribute("category", category);
        model.addAttribute("products", categoryService.getProductsByCategory(id));
        model.addAttribute("activeProducts", categoryService.getActiveProductsByCategory(id));
        model.addAttribute("totalProducts", categoryService.countProductsInCategory(id));
        model.addAttribute("activeProductsCount", categoryService.countActiveProductsInCategory(id));
        return "categories/detail";
    }

    @GetMapping("/{id}/products")
    public String viewProductsInCategory(@PathVariable int id, HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(id);
        if (category == null) return "redirect:/categories";
        model.addAttribute("category", category);
        model.addAttribute("products", categoryService.getProductsByCategory(id));
        return "categories/products";
    }

    @GetMapping("/{id}/products/active")
    public String viewActiveProductsInCategory(@PathVariable int id, HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(id);
        if (category == null) return "redirect:/categories";
        model.addAttribute("category", category);
        model.addAttribute("products", categoryService.getActiveProductsByCategory(id));
        return "categories/products";
    }

    @GetMapping("/new")
    public String showNewForm(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        model.addAttribute("category", new Category());
        return "categories/add";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               HttpServletRequest request,
                               Model model) {
        addSessionInfo(request, model);
        categoryService.save(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Category category = categoryService.getById(id);
        if (category == null) return "redirect:/categories";
        model.addAttribute("category", category);
        return "categories/edit";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable int id,
                                 @ModelAttribute("category") Category category,
                                 HttpServletRequest request,
                                 Model model) {
        addSessionInfo(request, model);
        category.setCategoryID(id);
        categoryService.save(category);
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable int id,
                                 HttpServletRequest request,
                                 Model model) {
        addSessionInfo(request, model);
        categoryService.delete(id);
        return "redirect:/categories";
    }
}
