package com.procurement.service;

import com.procurement.dto.request.MprRequest;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprDto;
import com.procurement.dto.responce.MprResponse;
import com.procurement.dto.responce.VenderDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MprRegServices {
    ResponseEntity<ApiResponse<MprDto>> mprReg(MprRequest request);
    ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprs();

}
