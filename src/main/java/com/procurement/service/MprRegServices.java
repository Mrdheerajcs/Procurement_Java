package com.procurement.service;

import com.procurement.dto.request.MprApprovalRequest;
import com.procurement.dto.request.MprRequest;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.dto.responce.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MprRegServices {
    ResponseEntity<ApiResponse<MprDto>> mprReg(MprRequest request);
    ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprs(String status);

    ResponseEntity<ApiResponse<MprDetailDTO>> mprApproval(MprApprovalRequest request);

}
