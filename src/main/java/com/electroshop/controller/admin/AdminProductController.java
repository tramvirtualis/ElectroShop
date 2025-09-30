package com.electroshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminProductController {

	@GetMapping("/admin/products")
	public String products() {
		return "admin/products";
	}
}


