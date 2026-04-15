package com.procurement.controller;

import com.procurement.config.VendorFeeConfig;
import com.procurement.entity.VendorPayment;
import com.procurement.repository.VendorPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VendorPaymentRepository paymentRepository;
    private final VendorFeeConfig feeConfig;

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