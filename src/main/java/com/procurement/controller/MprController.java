package com.procurement.controller;
import com.procurement.dto.request.MprRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprDto;
import com.procurement.service.MprRegServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/mpr")
@RequiredArgsConstructor
public class MprController {
    private final MprRegServices mprRegServices;
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<MprDto>> mprReg(@RequestBody MprRequest request) {
        log.info("Registering Mpr...");
        return mprRegServices.mprReg(request);
    }
}
