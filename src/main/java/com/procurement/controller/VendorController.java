package com.procurement.controller;
import com.procurement.dto.ApiResponse;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.dto.responce.AppsetupResponse;
import com.procurement.service.VendorRegService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {
  @Autowired
  VendorRegService vendorRegService;
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<AppsetupResponse>> venderReg(@RequestBody VenderRegRequest venderRegRequest) {
        log.info("Vender Registering......");
        return new ResponseEntity<>(vendorRegService.venReg(venderRegRequest), HttpStatus.OK);
    }

}