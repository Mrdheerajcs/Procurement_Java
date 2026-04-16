package com.procurement.repository;

import com.procurement.entity.BidTechnical;
import com.procurement.entity.TenderHeader;
import com.procurement.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidTechnicalRepository extends JpaRepository<BidTechnical, Long> {

    Optional<BidTechnical> findByTenderAndVendor(TenderHeader tender, Vendor vendor);
    boolean existsByTenderAndVendor(TenderHeader tender, Vendor vendor);
    List<BidTechnical> findByTenderAndEvaluationStatus(TenderHeader tender, String status);
    List<BidTechnical> findByTender(TenderHeader tender);
    Page<BidTechnical> findByTender(TenderHeader tender, Pageable pageable);
    List<BidTechnical> findByTenderAndSubmissionStatus(TenderHeader tender, String submissionStatus);
    List<BidTechnical> findByVendorAndEvaluationStatus(Vendor vendor, String evaluationStatus);

    @Query("SELECT b FROM BidTechnical b WHERE b.tender.tenderId = :tenderId AND b.evaluationStatus = 'QUALIFIED'")
    List<BidTechnical> findQualifiedBidsByTenderId(@Param("tenderId") Long tenderId);

    @Query("SELECT bt FROM BidTechnical bt WHERE bt.tender.tenderId = :tenderId")
    List<BidTechnical> findAllByTenderId(@Param("tenderId") Long tenderId);
}