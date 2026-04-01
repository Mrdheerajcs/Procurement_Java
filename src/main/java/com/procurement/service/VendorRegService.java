package com.procurement.service;

import com.procurement.dto.ApiResponse;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.dto.responce.AppsetupResponse;

public interface VendorRegService {
    ApiResponse<AppsetupResponse> venReg(VenderRegRequest venreq);
}
