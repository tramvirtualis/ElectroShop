package com.electroshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {

	@GetMapping("/products/{id}")
	public String productDetail(@PathVariable("id") Long id) {
		return "client/product-detail";
	}
}


