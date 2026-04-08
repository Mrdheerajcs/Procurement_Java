package com.procurement.controller;
import com.procurement.dto.request.*;
import com.procurement.dto.responce.*;
import com.procurement.service.MprRegServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @GetMapping("/getallbyMultiStatus")
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprDataByMultiStatus(
            @RequestParam List<String> status) {

        log.info("Fetching all Mpr with statuses: {}", status);
        return mprRegServices.getAllMprDataByMultiStatus(status);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateMpr(@RequestBody MprUpdateRequest request) {
        return mprRegServices.updateMpr(request);
    }
    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<String>>publishTender(
            @RequestPart("data") TenderRequest request,
            @RequestPart("files") List<MultipartFile> files) throws IOException {
        return mprRegServices.publishTender(request, files);
    }
}
