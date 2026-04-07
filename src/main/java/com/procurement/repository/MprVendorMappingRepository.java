package com.procurement.repository;
import com.procurement.entity.MprVendorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MprVendorMappingRepository extends JpaRepository<MprVendorMapping, Long> {
    void deleteByMprDetailMprDetailIdIn(List<Long> detailIds);
    void deleteByMprDetailMprDetailId(Long mprDetailId);
}
