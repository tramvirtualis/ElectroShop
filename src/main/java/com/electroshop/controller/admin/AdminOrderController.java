package com.electroshop.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminOrderController {

	@GetMapping("/admin/orders")
	public String orders() {
		return "admin/orders";
	}
}


