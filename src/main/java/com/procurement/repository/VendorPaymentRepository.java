package com.procurement.repository;

import com.procurement.entity.VendorPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorPaymentRepository extends JpaRepository<VendorPayment, Long> {

    Optional<VendorPayment> findTopByEmailOrderByPaymentIdDesc(String email);
}