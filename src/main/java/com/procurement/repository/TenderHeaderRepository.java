package com.procurement.repository;

import com.procurement.entity.TenderHeader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderHeaderRepository extends JpaRepository<TenderHeader, Long> {
}