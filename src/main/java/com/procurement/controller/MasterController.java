package com.procurement.controller;

import com.procurement.dto.request.*;
import com.procurement.dto.responce.*;
import com.procurement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterController {

    private final DepartmentService departmentService;
    private final MprTypeService mprTypeService;
    private final TenderTypeService tenderTypeService;
    private final VendorTypeService vendorTypeService;
    private final RoleService roleService;

    // ================== DEPARTMENT ==================
    @PostMapping("/createDepartment")
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(@RequestBody DepartmentRequest req) {
        return departmentService.create(req);
    }

    @PostMapping("/updateDepartment")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(@RequestBody DepartmentRequest req) {
        return departmentService.update(req);
    }

    @GetMapping("/getDepartmentById/{id}")
    public ResponseEntity<ApiResponse<DepartmentDto>> getDepartment(@PathVariable Integer id) {
        return departmentService.getById(id);
    }

    @GetMapping("/getAllDepartment")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAllDepartment() {
        return departmentService.getAll();
    }

    @PostMapping("/changeDepartmentStatus")
    public ResponseEntity<ApiResponse<String>> changeDepartmentStatus(@RequestParam Integer id, @RequestParam String status) {
        return departmentService.changeStatus(id, status);
    }

    // ================== MPR TYPE ==================
    @PostMapping("/createMprType")
    public ResponseEntity<ApiResponse<MprTypeDto>> createMprType(@RequestBody MprTypeRequest req) {
        return mprTypeService.create(req);
    }

    @PostMapping("/updateMprType")
    public ResponseEntity<ApiResponse<MprTypeDto>> updateMprType(@RequestBody MprTypeRequest req) {
        return mprTypeService.update(req);
    }

    @GetMapping("/getMprTypeById/{id}")
    public ResponseEntity<ApiResponse<MprTypeDto>> getMprType(@PathVariable Long id) {
        return mprTypeService.getById(id);
    }

    @GetMapping("/getAllMprType")
    public ResponseEntity<ApiResponse<List<MprTypeDto>>> getAllMprType() {
        return mprTypeService.getAll();
    }

    @PostMapping("/changeMprTypeStatus")
    public ResponseEntity<ApiResponse<String>> changeMprTypeStatus(@RequestParam Long id, @RequestParam String status) {
        return mprTypeService.changeStatus(id, status);
    }


    // ================== ROLE ==================

    @PostMapping("/createRole")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody RoleRequest req) {
        return roleService.create(req);
    }

    @PostMapping("/updateRole")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(@RequestBody RoleRequest req) {
        return roleService.update(req);
    }

    @GetMapping("/getRoleById/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getRole(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @GetMapping("/getAllRole")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRole() {
        return roleService.getAll();
    }

    @PostMapping("/changeRoleStatus")
    public ResponseEntity<ApiResponse<String>> changeRoleStatus(
            @RequestParam Long id,
            @RequestParam String status) {
        return roleService.changeStatus(id, status);
    }


//    ============================= Tender Type =====================

    @PostMapping("/createTenderType")
    public ResponseEntity<ApiResponse<TenderTypeDto>> create(@RequestBody TenderTypeRequest request) {
        return tenderTypeService.create(request);
    }

    @PostMapping("/updateTenderType")
    public ResponseEntity<ApiResponse<TenderTypeDto>> update(@RequestBody TenderTypeRequest request) {
        return tenderTypeService.update(request);
    }

    @GetMapping("/getTenderTypeById/{id}")
    public ResponseEntity<ApiResponse<TenderTypeDto>> getById(@PathVariable Long id) {
        return tenderTypeService.getById(id);
    }

    @GetMapping("/getAllTenderType")
    public ResponseEntity<ApiResponse<List<TenderTypeDto>>> getAll() {
        return tenderTypeService.getAll();
    }

    @PostMapping("/changeTenderTypeStatus")
    public ResponseEntity<ApiResponse<String>> changeStatus(
            @RequestParam Long id,
            @RequestParam String status
    ) {
        return tenderTypeService.changeStatus(id, status);
    }


// ================== VENDOR TYPE ==================

    @PostMapping("/createVendorType")
    public ResponseEntity<ApiResponse<VendorTypeDto>> createVendorType(@RequestBody VendorTypeRequest req) {
        return vendorTypeService.create(req);
    }

    @PostMapping("/updateVendorType")
    public ResponseEntity<ApiResponse<VendorTypeDto>> updateVendorType(@RequestBody VendorTypeRequest req) {
        return vendorTypeService.update(req);
    }

    @GetMapping("/getVendorTypeById/{id}")
    public ResponseEntity<ApiResponse<VendorTypeDto>> getVendorType(@PathVariable Long id) {
        return vendorTypeService.getById(id);
    }

    @GetMapping("/getAllVendorType")
    public ResponseEntity<ApiResponse<List<VendorTypeDto>>> getAllVendorType() {
        return vendorTypeService.getAll();
    }

    @PostMapping("/changeVendorTypeStatus")
    public ResponseEntity<ApiResponse<String>> changeVendorTypeStatus(
            @RequestParam Long id,
            @RequestParam String status) {
        return vendorTypeService.changeStatus(id, status);
    }

}