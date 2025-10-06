package com.electroshop;

import com.electroshop.controller.client.HomeController;
import com.electroshop.entity.Product;
import com.electroshop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HomeController.class)
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void testHomePageWithProducts() throws Exception {
        // Create sample products
        Product product1 = new Product();
        product1.setId(1L);
        product1.setProductName("Samsung Galaxy S24");
        product1.setDescription("Latest Samsung smartphone with advanced features");
        product1.setProductPrice(new BigDecimal("999.99"));
        product1.setStockQuantity(50);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setProductName("MacBook Pro 14\"");
        product2.setDescription("Apple MacBook Pro with M3 chip");
        product2.setProductPrice(new BigDecimal("1999.99"));
        product2.setStockQuantity(25);

        List<Product> products = Arrays.asList(product1, product2);

        // Mock the service
        when(productService.getVisibleProducts()).thenReturn(products);

        // Test the home page
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/index"))
                .andExpect(model().attribute("products", products))
                .andExpect(model().attribute("productCount", 2))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Samsung Galaxy S24")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MacBook Pro 14\"")));
    }

    @Test
    void testHomePageWithNoProducts() throws Exception {
        // Mock empty product list
        when(productService.getVisibleProducts()).thenReturn(Arrays.asList());

        // Test the home page
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/index"))
                .andExpect(model().attribute("products", Arrays.asList()))
                .andExpect(model().attribute("productCount", 0))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No Products Available")));
    }
}
