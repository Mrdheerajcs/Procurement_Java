package com.procurement.repository;

import com.procurement.entity.TenderDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenderDocumentRepository extends JpaRepository<TenderDocument, Long> {
}
