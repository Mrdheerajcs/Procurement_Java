package com.procurement.repository;

import com.procurement.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    @Query("SELECT MAX(v.vendorId) FROM Vendor v")
    Long findMaxVendorId();

    boolean existsByVendorCode(String vendorCode);

    Optional<Vendor> findByEmailId(String username);
}