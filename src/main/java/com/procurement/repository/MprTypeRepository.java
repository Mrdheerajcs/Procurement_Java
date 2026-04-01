package com.procurement.repository;

import com.procurement.entity.MprHeader;
import com.procurement.entity.MprType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MprTypeRepository extends JpaRepository<MprType, Long> {
}
