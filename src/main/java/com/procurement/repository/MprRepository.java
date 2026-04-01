package com.procurement.repository;

import com.procurement.entity.MprHeader;
import com.procurement.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MprRepository extends JpaRepository<MprHeader, Long> {
}
