package com.procurement.mapper;

import com.procurement.dto.responce.CountryDto;
import com.procurement.entity.Country;
import org.springframework.stereotype.Component;

@Component
public class CountryMapper {

    // 🔹 Entity → DTO
    public  CountryDto toDTO(Country country) {
        if (country == null) {
            return null;
        }

        CountryDto dto = new CountryDto();
        dto.setCountryId(country.getCountryId());
        dto.setCountryName(country.getCountryName());
        dto.setCountryCode(country.getCountryCode());
        dto.setCreatedAt(country.getCreatedBy());

        return dto;
    }
    // 🔹 DTO → Entity
    public static Country toEntity(CountryDto dto) {
        if (dto == null) {
            return null;
        }
        Country country = new Country();
        country.setCountryId(dto.getCountryId());
        country.setCountryName(dto.getCountryName());
        country.setCountryCode(dto.getCountryCode());
        country.setCreatedBy(dto.getCreatedAt());
        return country;
    }
}
