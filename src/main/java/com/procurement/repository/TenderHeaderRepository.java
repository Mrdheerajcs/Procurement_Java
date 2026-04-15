package com.procurement.repository;

import com.procurement.entity.TenderHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenderHeaderRepository extends JpaRepository<TenderHeader, Long> {

    List<TenderHeader> findByTenderStatus(String status);
    List<TenderHeader> findByStatus(String status);

}