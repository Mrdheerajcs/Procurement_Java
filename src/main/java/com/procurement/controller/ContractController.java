package com.procurement.controller;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.entity.Contract;
import com.procurement.entity.Vendor;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.ContractRepository;
import com.procurement.repository.VendorRepository;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;

    @GetMapping("/my-contracts")
    public ResponseEntity<ApiResponse<List<Contract>>> getMyContracts() {
        log.info("Fetching contracts for logged-in vendor");
        try {
            String username = CurrentUser.getCurrentUserOrThrow().getUsername();
            Vendor vendor = vendorRepository.findByEmailId(username)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            List<Contract> contracts = contractRepository.findByVendorId(vendor.getVendorId());
            return ResponseUtil.success(contracts, "Contracts retrieved");
        } catch (Exception e) {
            log.error("Error fetching contracts: {}", e.getMessage());
            return ResponseUtil.success(List.of(), "No contracts found");
        }
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<Contract>> getContractById(@PathVariable Long contractId) {
        log.info("Fetching contract by ID: {}", contractId);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        return ResponseUtil.success(contract, "Contract retrieved");
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Contract>>> getAllContracts() {
        log.info("Fetching all contracts");
        List<Contract> contracts = contractRepository.findAll();
        return ResponseUtil.success(contracts, "All contracts retrieved");
    }
}