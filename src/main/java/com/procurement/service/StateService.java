package com.procurement.service;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.StateDTO;
import com.procurement.dto.responce.VendorTypeDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface StateService {

    List<StateDTO> getStatesByCountryId(Long countryId);
}
