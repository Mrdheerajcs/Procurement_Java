package com.procurement.repository;
import com.procurement.entity.MprVendorMapping;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Transactional
public interface MprVendorMappingRepository extends JpaRepository<MprVendorMapping, Long> {
    @Modifying
    @Query(value = "DELETE FROM mpr_vendor_mapping WHERE mpr_detail_id = :detailId", nativeQuery = true)
    void deleteByMprDetailId(@Param("detailId") Long detailId);
}
