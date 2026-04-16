package com.procurement.repository;

import com.procurement.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByVendorId(Long vendorId);
    List<Contract> findByVendorIdAndStatus(Long vendorId, String status);
}