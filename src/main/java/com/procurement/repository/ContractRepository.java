package com.procurement.repository;

import com.procurement.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByVendorId(Long vendorId);
    List<Contract> findByVendorIdAndStatus(Long vendorId, String status);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contract c WHERE c.status = 'AWARDED'")
    BigDecimal sumAwardedAmount();

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = 'AWARDED'")
    long countAwardedContracts();

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = 'AWARDED' AND c.endDate BETWEEN CURRENT_DATE AND CURRENT_DATE + 90")
    long countContractsEndingInNext90Days();

    Optional<Contract> findByContractNo(String contractNo);


    List<Contract> findByStatus(String status);

    List<Contract> findByTenderId(Long tenderId);

    boolean existsByContractNo(String contractNo);
}