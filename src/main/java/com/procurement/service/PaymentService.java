package com.procurement.service;

import com.procurement.dto.request.PaymentRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.PaymentResponse;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    ResponseEntity<ApiResponse<PaymentResponse>> createOrder(PaymentRequest request);
    ResponseEntity<ApiResponse<String>> verifyPayment(String orderId, String paymentId, String signature);
    ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(String orderId);
}