package com.procurement.controller;

import com.procurement.config.VendorFeeConfig;
import com.procurement.dto.request.PaymentRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.PaymentResponse;
import com.procurement.entity.VendorPayment;
import com.procurement.repository.VendorPaymentRepository;
import com.procurement.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VendorPaymentRepository paymentRepository;
    private final VendorFeeConfig feeConfig;

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentResponse>> createOrder(@RequestBody PaymentRequest request) {
        log.info("Creating payment order for type: {}", request.getPaymentType());
        return paymentService.createOrder(request);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyPayment(
            @RequestParam String orderId,
            @RequestParam String paymentId,
            @RequestParam String signature) {
        log.info("Verifying payment for order: {}", orderId);
        return paymentService.verifyPayment(orderId, paymentId, signature);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(@PathVariable String orderId) {
        log.info("Getting payment status for: {}", orderId);
        return paymentService.getPaymentStatus(orderId);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, String> req) {

        VendorPayment payment = new VendorPayment();
        payment.setEmail(req.get("email"));
        payment.setAmount(feeConfig.getAmount());
        payment.setStatus("PENDING");

        paymentRepository.save(payment);

        return ResponseEntity.ok(payment);
    }

    @PostMapping("/success")
    public ResponseEntity<?> paymentSuccess(@RequestBody Map<String, Long> req) {

        VendorPayment payment = paymentRepository.findById(req.get("paymentId"))
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN" + System.currentTimeMillis());

        paymentRepository.save(payment);

        return ResponseEntity.ok("Payment success");
    }
}