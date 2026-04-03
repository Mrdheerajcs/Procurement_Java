package com.procurement.service.impl;
import com.procurement.dto.responce.StateDTO;
import com.procurement.entity.State;
import com.procurement.mapper.StateMapper;
import com.procurement.repository.StateRepository;
import com.procurement.service.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateServiceImpl implements StateService {
    private final StateRepository stateRepository;
    private final StateMapper stateMapper;

    @Override
    public List<StateDTO> getStatesByCountryId(Long countryId) {
        List<State> states = stateRepository.findByCountryCountryId(countryId);
        if (states.isEmpty()) {
            throw new RuntimeException("No data found for given countryId: " + countryId);
        }
        return states.stream()
                .map(stateMapper::toDto)
                .collect(Collectors.toList());
    }
}
