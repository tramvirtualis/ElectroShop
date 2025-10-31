package com.hometech.hometech.controller.Thymleaf;
import org.springframework.ui.Model;
import com.hometech.hometech.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final VnPayService vnPayService;

    public PaymentController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @GetMapping
    public String showPaymentPage() {
        return "payment/payment"; // file payment.html trong /templates
    }

    @GetMapping("/vnpay")
    public String pay(HttpServletRequest request) {
        // Lấy số tiền, ở đây test cố định 10000đ
        long amount = 10000;
        String orderInfo = "Thanh toán đơn hàng thử nghiệm";

        // Tạo URL thanh toán VNPAY
        String paymentUrl = vnPayService.createOrder(request, amount, orderInfo);

        // In ra để kiểm tra
        System.out.println("🔗 Redirect URL: " + paymentUrl);

        // ⚠️ QUAN TRỌNG: PHẢI redirect
        return "redirect:" + paymentUrl;
    }

    @GetMapping("/vnpay-return")
    public String paymentReturn(HttpServletRequest request, Model model) {
        int result = vnPayService.processReturn(request);
        model.addAttribute("status", result == 1 ? "success" : "fail");
        return "payment/payment-result";
    }
}
