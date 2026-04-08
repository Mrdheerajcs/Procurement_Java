package com.procurement.repository;

import com.procurement.entity.MprDetail;
import com.procurement.entity.MprHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MprDetailRepository extends JpaRepository<MprDetail, Long> {
    List<MprDetail> findByMprHeader(MprHeader headerId);
    List<MprDetail> findByMprHeaderMprId(Long headerId);
    List< MprDetail> findByMprDetailId(Long detailId);

    @Query("SELECT d FROM MprDetail d JOIN FETCH d.mprHeader h WHERE LOWER(d.status) = LOWER(:status)")
    List<MprDetail> findByStatusWithHeader(@Param("status") String status);

    @Query("SELECT d FROM MprDetail d JOIN FETCH d.mprHeader WHERE d.status IN :statuses")
    List<MprDetail> findByMultiStatusWithHeader(@Param("statuses") List<String> statuses);

}
