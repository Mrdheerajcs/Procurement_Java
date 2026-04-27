package com.procurement.repository;

import com.procurement.entity.MprHeader;
import com.procurement.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MprRepository extends JpaRepository<MprHeader, Long> {
    List<MprHeader> findByApprovalStatusAndApprovalLevel(String approvalStatus, String approvalLevel);
    List<MprHeader> findByCreatedByAndApprovalStatus(String createdBy, String approvalStatus);

    long countByStatus(String status);

    long countByApprovalStatus(String approvalStatus);

    @Query("SELECT COUNT(m) FROM MprHeader m WHERE m.approvalStatus = 'APPROVED' AND m.approvalLevel = 'COMPLETED'")
    long countFullyApproved();

    boolean existsByMprNo(String mprNo);

    @Query("SELECT MAX(CAST(SUBSTRING(m.mprNo, LENGTH(:prefix) + 1) AS long)) FROM MprHeader m WHERE m.mprNo LIKE CONCAT(:prefix, '%')")
    Long getLastSequenceForPrefix(@Param("prefix") String prefix);
}
