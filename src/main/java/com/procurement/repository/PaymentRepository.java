package com.procurement.repository;

import com.procurement.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByRazorpayOrderId(String orderId);
    Optional<PaymentEntity> findByRazorpayPaymentId(String paymentId);
    List<PaymentEntity> findByVendorId(Long vendorId);
    List<PaymentEntity> findByTenderId(Long tenderId);
    List<PaymentEntity> findByContractId(Long contractId);
    List<PaymentEntity> findByStatus(String status);
}