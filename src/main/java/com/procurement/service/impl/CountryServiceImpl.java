package com.procurement.service.impl;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.CountryDto;
import com.procurement.mapper.CountryMapper;
import com.procurement.repository.CountryRepository;
import com.procurement.service.CountryService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;
    @Override
    public ResponseEntity<ApiResponse<List<CountryDto>>> getAll() {
        List<CountryDto> list = countryRepository.findAll()
                .stream()
                .map(countryMapper::toDTO)
                .toList();
        return ResponseUtil.success(list, "Success");
    }
}
