package com.procurement.controller;
import com.procurement.dto.request.MprApprovalRequest;
import com.procurement.dto.request.MprRequest;
import com.procurement.dto.responce.*;
import com.procurement.service.MprRegServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

//    @GetMapping("/getall")
//    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprData() {
//        log.info("Fetching all Mpr...");
//        return mprRegServices.getAllMprs();
//    }
    @PutMapping("/approve")
    public ResponseEntity<ApiResponse<MprDetailDTO>> mprApproval(@RequestBody MprApprovalRequest request) {
        log.info("Mpr approval...");
        return mprRegServices.mprApproval(request);
    }

    @GetMapping("/getallbyStatus")
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprData(
            @RequestParam String status) {
        log.info("Fetching all Mpr with status: {}", status);
        return mprRegServices.getAllMprs(status);
    }



}
