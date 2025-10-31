package com.hometech.hometech.controller.Thymleaf;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.User;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AccountReposirory accountRepository;
    private final UserRepository userRepository;

    public HomeController(ProductService productService, CategoryService categoryService,
                          AccountReposirory accountRepository, UserRepository userRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model, 
                      @RequestParam(required = false) String category,
                      @RequestParam(required = false) String sort) {
        // Normalize empty sort parameter to null
        if (sort != null && sort.trim().isEmpty()) {
            sort = null;
        }
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
        
        // Sort products by price if sort parameter is provided
        if (sort != null && !sort.trim().isEmpty()) {
            if ("price_asc".equals(sort)) {
                displayProducts.sort((p1, p2) -> {
                    double price1 = p1.getPrice();
                    double price2 = p2.getPrice();
                    return Double.compare(price1, price2);
                });
            } else if ("price_desc".equals(sort)) {
                displayProducts.sort((p1, p2) -> {
                    double price1 = p1.getPrice();
                    double price2 = p2.getPrice();
                    return Double.compare(price2, price1);
                });
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("topSelling", displayProducts);
        model.addAttribute("title", "Trang chủ - HomeTech");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeCategoryId", activeCategoryId);
        model.addAttribute("selectedCategory", category != null ? category : "all");
        model.addAttribute("selectedSort", (sort != null && !sort.trim().isEmpty()) ? sort : "");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            model.addAttribute("currentUser", auth.getName());
            String name = auth.getName();
            Account account = accountRepository.findByUsername(name)
                    .or(() -> accountRepository.findByEmail(name))
                    .orElse(null);
            if (account == null && auth instanceof OAuth2AuthenticationToken oAuth) {
                Object principal = oAuth.getPrincipal();
                if (principal instanceof OAuth2User oUser) {
                    Object emailAttr = oUser.getAttributes().get("email");
                    if (emailAttr != null) {
                        account = accountRepository.findByEmail(String.valueOf(emailAttr)).orElse(null);
                    }
                }
            }
            if (account != null) {
                User u = userRepository.findByAccount(account);
                if (u != null) {
                    if (u.getFullName() != null && !u.getFullName().isEmpty()) {
                        model.addAttribute("currentUserName", u.getFullName());
                    } else if (u.getName() != null && !u.getName().isEmpty()) {
                        model.addAttribute("currentUserName", u.getName());
                    } else if (account.getEmail() != null) {
                        model.addAttribute("currentUserName", account.getEmail());
                    }
                }
            }
        }
        return "home";
    }

    // 🔎 Search products from the home page search bar
    @GetMapping("/search")
    public String search(Model model, @RequestParam("keyword") String keyword) {
        List<Category> categories = categoryService.getAll();
        List<Product> newProducts = productService.getProductsAddedInLast7Days();
        List<Product> results = productService.searchByName(keyword);
        model.addAttribute("categories", categories);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("topSelling", results); // reuse grid
        model.addAttribute("pageTitle", "Kết quả tìm kiếm: " + keyword);
        model.addAttribute("activeCategoryId", null);
        model.addAttribute("title", "Tìm kiếm - HomeTech");
        return "home";
    }
}
