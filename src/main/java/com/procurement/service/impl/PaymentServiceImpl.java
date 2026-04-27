package com.procurement.service.impl;

import com.procurement.dto.request.PaymentRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.PaymentResponse;
import com.procurement.entity.PaymentEntity;
import com.procurement.entity.VendorPayment;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.PaymentRepository;
import com.procurement.repository.VendorPaymentRepository;
import com.procurement.service.PaymentService;
import com.procurement.util.ResponseUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final VendorPaymentRepository vendorPaymentRepository;

    @Value("${razorpay.key.id:rzp_test_xxxxxxxx}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:your_secret_here}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<PaymentResponse>> createOrder(PaymentRequest request) {
        log.info("Creating payment order for amount: {}", request.getAmount());

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(new java.math.BigDecimal(100)).longValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);

            // Get current user info
            String currentUser = "";
            try {
                currentUser = CurrentUser.getCurrentUserOrThrow().getUsername();
            } catch (Exception e) {
                currentUser = request.getEmail();
            }

            // Save payment record
            PaymentEntity payment = PaymentEntity.builder()
                    .razorpayOrderId(order.get("id"))
                    .tenderId(request.getTenderId())
                    .contractId(request.getContractId())
                    .vendorEmail(currentUser)
                    .vendorName(request.getName())
                    .paymentType(request.getPaymentType())
                    .amount(request.getAmount())
                    .currency("INR")
                    .status("CREATED")
                    .receipt(order.get("receipt"))
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            // If vendor registration payment, update VendorPayment table
            if ("VENDOR_REGISTRATION".equals(request.getPaymentType())) {
                VendorPayment vendorPayment = new VendorPayment();
                vendorPayment.setEmail(request.getEmail());
                vendorPayment.setAmount(request.getAmount().doubleValue());
                vendorPayment.setStatus("PENDING");
                vendorPayment.setTransactionId(order.get("id"));
                vendorPaymentRepository.save(vendorPayment);
            }

            PaymentResponse response = PaymentResponse.builder()
                    .razorpayOrderId(order.get("id"))
                    .razorpayKeyId(razorpayKeyId)
                    .amount(request.getAmount().longValue())
                    .currency("INR")
                    .receipt(order.get("receipt"))
                    .status(order.get("status"))
                    .build();

            return ResponseUtil.success(response, "Order created successfully");

        } catch (RazorpayException e) {
            log.error("Razorpay error: {}", e.getMessage());
            return ResponseUtil.error("Failed to create order: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return ResponseUtil.error("Failed to create order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> verifyPayment(String orderId, String paymentId, String signature) {
        log.info("Verifying payment for order: {}", orderId);

        try {
            PaymentEntity payment = paymentRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment record not found"));

            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (isValid) {
                payment.setRazorpayPaymentId(paymentId);
                payment.setRazorpaySignature(signature);
                payment.setStatus("PAID");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Update VendorPayment if vendor registration
                if ("VENDOR_REGISTRATION".equals(payment.getPaymentType())) {
                    VendorPayment vendorPayment = vendorPaymentRepository
                            .findTopByEmailOrderByPaymentIdDesc(payment.getVendorEmail())
                            .orElse(null);
                    if (vendorPayment != null) {
                        vendorPayment.setStatus("SUCCESS");
                        vendorPayment.setTransactionId(paymentId);
                        vendorPaymentRepository.save(vendorPayment);
                    }
                }

                return ResponseUtil.success("Payment verified successfully");
            } else {
                payment.setStatus("FAILED");
                payment.setErrorMessage("Signature verification failed");
                paymentRepository.save(payment);
                return ResponseUtil.error("Payment verification failed");
            }

        } catch (RazorpayException e) {
            log.error("Verification error: {}", e.getMessage());
            return ResponseUtil.error("Verification failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(String orderId) {
        log.info("Getting payment status for order: {}", orderId);

        PaymentEntity payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        PaymentResponse response = PaymentResponse.builder()
                .razorpayOrderId(payment.getRazorpayOrderId())
                .amount(payment.getAmount().longValue())
                .currency(payment.getCurrency())
                .status("PAID".equals(payment.getStatus()) ? 1 : 0)
                .build();

        return ResponseUtil.success(response, "Payment status retrieved");
    }
}