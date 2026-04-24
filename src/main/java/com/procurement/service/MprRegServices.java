package com.procurement.service;

import com.procurement.dto.request.*;
import com.procurement.dto.responce.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MprRegServices {
    ResponseEntity<ApiResponse<MprDto>> mprReg(MprRequest request);
    ResponseEntity<ApiResponse<MprDetailDTO>> mprApproval(MprApprovalRequest request);
    ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprs(String status);
    ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprDataByMultiStatus(List<String> statuses);
    ResponseEntity<ApiResponse<String>> updateMpr(MprUpdateRequest request);
    ResponseEntity<ApiResponse<String>> publishTender(TenderRequest request, MultipartFile nitDoc, MultipartFile boqDoc, MultipartFile techDoc, List<MultipartFile> otherDocs) throws IOException;

    ResponseEntity<ApiResponse<List<MprDocumentDto>>> getMprDocuments(Long mprId);
    void updateDocumentPath(Long mprId, String documentPath);
}