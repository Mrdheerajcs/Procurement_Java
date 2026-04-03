package com.procurement.repository;

import com.procurement.entity.Country;
import com.procurement.entity.VendorType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country,Long> {
}
