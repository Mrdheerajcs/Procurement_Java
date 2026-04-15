package com.procurement.repository;

import com.procurement.entity.MprHeader;
import com.procurement.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MprRepository extends JpaRepository<MprHeader, Long> {
    List<MprHeader> findByApprovalStatusAndApprovalLevel(String approvalStatus, String approvalLevel);
    List<MprHeader> findByCreatedByAndApprovalStatus(String createdBy, String approvalStatus);
}
