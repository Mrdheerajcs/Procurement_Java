package com.procurement.repository;

import com.procurement.entity.MprApprovalHistory;
import com.procurement.entity.MprHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MprApprovalHistoryRepository extends JpaRepository<MprApprovalHistory, Long> {
    List<MprApprovalHistory> findByMprHeaderOrderByActionAtAsc(MprHeader mprHeader);
}