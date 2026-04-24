package com.procurement.repository;

import com.procurement.entity.TenderDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenderDocumentRepository extends JpaRepository<TenderDocument, Long> {
    List<TenderDocument> findByTenderId(Long tenderId);
}
