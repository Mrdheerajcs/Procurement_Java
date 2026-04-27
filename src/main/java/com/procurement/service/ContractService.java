package com.procurement.service;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.ContractDTO;
import com.procurement.dto.request.ContractRequest;
import com.procurement.dto.responce.WorkOrderDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractService {

    ResponseEntity<ApiResponse<ContractDTO>> createContract(ContractRequest request);

    ResponseEntity<ApiResponse<ContractDTO>> getContractById(Long contractId);

    ResponseEntity<ApiResponse<List<ContractDTO>>> getAllContracts();

    ResponseEntity<ApiResponse<List<ContractDTO>>> getMyContracts();

    ResponseEntity<ApiResponse<ContractDTO>> updateContractStatus(Long contractId, String status);

    ResponseEntity<ApiResponse<String>> uploadPBG(Long contractId, MultipartFile file);

    ResponseEntity<ApiResponse<byte[]>> downloadWorkOrder(Long contractId);

    ResponseEntity<ApiResponse<WorkOrderDTO>> viewWorkOrder(Long contractId);

    ResponseEntity<ApiResponse<List<ContractDTO>>> getContractsByStatus(String status);

    ResponseEntity<ApiResponse<byte[]>> generateWorkOrderPDF(Long contractId);
}