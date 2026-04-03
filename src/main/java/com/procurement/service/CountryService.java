package com.procurement.service;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.CountryDto;
import com.procurement.dto.responce.VendorTypeDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CountryService {
    ResponseEntity<ApiResponse<List<CountryDto>>> getAll();
}
