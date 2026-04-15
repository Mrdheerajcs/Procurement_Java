package com.procurement.repository;

import com.procurement.entity.BidFinancial;
import com.procurement.entity.BidTechnical;
import com.procurement.entity.TenderHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidFinancialRepository extends JpaRepository<BidFinancial, Long> {

    Optional<BidFinancial> findByBidTechnical(BidTechnical bidTechnical);

    List<BidFinancial> findByTender(TenderHeader tender);

    @Query("SELECT bf FROM BidFinancial bf WHERE bf.tender.tenderId = :tenderId AND bf.isRevealed = 'YES'")
    List<BidFinancial> findRevealedFinancialsByTenderId(@Param("tenderId") Long tenderId);

    @Modifying
    @Transactional
    @Query("UPDATE BidFinancial bf SET bf.isRevealed = 'YES', bf.revealedBy = :revealedBy, bf.revealedAt = CURRENT_TIMESTAMP WHERE bf.bidTechnical.bidTechnicalId = :bidTechnicalId")
    void revealFinancial(@Param("bidTechnicalId") Long bidTechnicalId, @Param("revealedBy") String revealedBy);
}