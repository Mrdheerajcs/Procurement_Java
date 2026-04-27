package com.procurement.repository;

import com.procurement.entity.VendorPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorPaymentRepository extends JpaRepository<VendorPayment, Long> {

    Optional<VendorPayment> findTopByEmailOrderByPaymentIdDesc(String email);

    Optional<VendorPayment> findByTransactionId(String transactionId);

    // ✅ These will work now because fields exist in VendorPayment
    Optional<VendorPayment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<VendorPayment> findByRazorpayPaymentId(String razorpayPaymentId);
}