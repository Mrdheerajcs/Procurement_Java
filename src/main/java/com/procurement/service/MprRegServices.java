package com.procurement.service;

import com.procurement.dto.request.MprRequest;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprDto;
import com.procurement.dto.responce.VenderDto;
import org.springframework.http.ResponseEntity;

public interface MprRegServices {
    ResponseEntity<ApiResponse<MprDto>> mprReg(MprRequest request);

}
