package com.procurement.repository;
import com.procurement.entity.MprVendorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MprVendorMappingRepository extends JpaRepository<MprVendorMapping, Long> {
}
