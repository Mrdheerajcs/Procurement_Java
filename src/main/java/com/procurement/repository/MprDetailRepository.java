package com.procurement.repository;

import com.procurement.dto.request.MprDetailRequest;
import com.procurement.entity.MprDetail;
import com.procurement.entity.MprHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MprDetailRepository extends JpaRepository<MprDetail, Long> {
    List<MprDetail> findByMprHeader(MprHeader headerId);
}
