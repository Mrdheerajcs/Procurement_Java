package com.procurement.mapper;

import com.procurement.dto.responce.StateDTO;
import com.procurement.entity.Country;
import com.procurement.entity.State;
import com.procurement.repository.CountryRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
public class StateMapper {

    // 🔹 Entity → DTO
    public  StateDTO toDto(State state) {
        if (state == null) {
            return null;
        }
        StateDTO dto = new StateDTO();
        dto.setStateId(state.getStateId());
        dto.setStateName(state.getStateName());
        dto.setStateCode(state.getStateCode());
        // 🔗 Country mapping
        if (state.getCountry() != null) {
            dto.setCountryId(state.getCountry().getCountryId());
        }
        dto.setCreatedAt(state.getCreatedAt());
        return dto;
    }
    // 🔹 DTO → Entity
    public  State toDto(StateDTO dto, CountryRepository countryRepository) {
        if (dto == null) {
            return null;
        }
        State state = new State();
        state.setStateId(dto.getStateId());
        state.setStateName(dto.getStateName());
        state.setStateCode(dto.getStateCode());
        state.setCreatedAt(dto.getCreatedAt());
        //  Country mapping (Important)
//        if (dto.getCountryId() != null) {
//            Country country = countryRepository.findById(dto.getCountryId())
//                    .orElseThrow(() -> new RuntimeException("Country not found with id: " + dto.getCountryId()));
//            state.setCountry(country);
//        }
        return state;
    }

    //  Update existing entity   till now not use
    public static void updateEntityFromDTO(StateDTO dto, State entity, CountryRepository countryRepository) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setStateName(dto.getStateName());
        entity.setStateCode(dto.getStateCode());

        if (dto.getCountryId() != null) {
            Country country = countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException("Country not found with id: " + dto.getCountryId()));

            entity.setCountry(country);
        }
    }
}