package com.procurement.repository;

import com.procurement.entity.TenderHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TenderHeaderRepository extends JpaRepository<TenderHeader, Long> {

    List<TenderHeader> findByTenderStatus(String status);
    List<TenderHeader> findByStatus(String status);

    @Query("SELECT COUNT(t) FROM TenderHeader t WHERE FUNCTION('MONTH', t.publishDate) = :month AND FUNCTION('YEAR', t.publishDate) = :year")
    long countTendersPublishedInMonth(@Param("month") int month, @Param("year") int year);

    // ✅ NEW: Get closed tenders (bidEndDate < today)
    @Query("SELECT COUNT(t) FROM TenderHeader t WHERE t.bidEndDate < CURRENT_DATE AND t.tenderStatus != 'CLOSED'")
    long countClosedTenders();

}