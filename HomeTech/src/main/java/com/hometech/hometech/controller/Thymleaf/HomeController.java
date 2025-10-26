package com.hometech.hometech.controller.Thymeleaf;

import com.hometech.hometech.model.Product;
import com.hometech.hometech.model.Category;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Category> categories = categoryService.getAll();
        List<Product> newProducts = productService.getProductsAddedInLast7Days();
        List<Product> topSelling = productService.getTop10BestSellingProducts();

        model.addAttribute("categories", categories);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("topSelling", topSelling);
        model.addAttribute("title", "Trang chủ - HomeTech");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            model.addAttribute("currentUser", auth.getName());
        }
        return "home"; // Trỏ tới templates/home.html
    }
}
