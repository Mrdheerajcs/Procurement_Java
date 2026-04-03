package com.procurement.dto.responce;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CountryDto {
    private Long countryId;
    private String countryName;
    private String countryCode;
    private LocalDateTime createdAt;
}
