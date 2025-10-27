package com.hometech.hometech.controller.Thymleaf;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) String category) {
        // Get all categories for navigation
        List<Category> categories = categoryService.getAll();
        List<Product> newProducts = productService.getProductsAddedInLast7Days();
        
        // Get products to display based on category filter
        List<Product> displayProducts;
        String pageTitle = "Sản phẩm bán chạy";
        Integer activeCategoryId = null;
        
        if (category != null && !category.trim().isEmpty() && !category.equals("all")) {
            // Filter by category ID
            try {
                int categoryId = Integer.parseInt(category);
                displayProducts = productService.getProductsByCategoryId(categoryId);
                
                // Find the active category for highlighting
                Category activeCategory = categories.stream()
                    .filter(c -> c.getCategoryID() == categoryId)
                    .findFirst()
                    .orElse(null);
                
                if (activeCategory != null) {
                    activeCategoryId = categoryId;
                    pageTitle = "Danh mục: " + activeCategory.getCategoryName();
                }
            } catch (NumberFormatException e) {
                // If category param is not a number, get all products
                displayProducts = productService.getTop10BestSellingProducts();
            }
        } else {
            // Show all products if no category selected
            displayProducts = productService.getTop10BestSellingProducts();
        }

        model.addAttribute("categories", categories);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("topSelling", displayProducts);
        model.addAttribute("title", "Trang chủ - HomeTech");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeCategoryId", activeCategoryId);
        model.addAttribute("selectedCategory", category != null ? category : "all");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            model.addAttribute("currentUser", auth.getName());
        }
        return "home";
    }
}
