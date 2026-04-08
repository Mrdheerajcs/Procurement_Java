package com.procurement.service;

import com.procurement.dto.request.*;
import com.procurement.dto.responce.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface MprRegServices {
    ResponseEntity<ApiResponse<MprDto>> mprReg(MprRequest request);
    ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprs(String status);

    ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprDataByMultiStatus(List<String> statuses);

    ResponseEntity<ApiResponse<MprDetailDTO>> mprApproval(MprApprovalRequest request);
    ResponseEntity<ApiResponse<String>> updateMpr(MprUpdateRequest request);

    ResponseEntity<ApiResponse<String>> publishTender(TenderRequest request, List<MultipartFile> files) throws IOException;
}
