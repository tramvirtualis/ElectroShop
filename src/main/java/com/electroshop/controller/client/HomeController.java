package com.electroshop.controller.client;

import com.electroshop.entity.Product;
import com.electroshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String index(Model model) {
        // Fetch visible products from the database
        List<Product> products = productService.getVisibleProducts();
        
        // Add products to the model for Thymeleaf
        model.addAttribute("products", products);
        
        // Add product count for debugging/info
        model.addAttribute("productCount", products.size());
        
        return "client/index";
    }
}


