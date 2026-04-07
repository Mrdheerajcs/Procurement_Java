package com.procurement.service.impl;
import com.procurement.dto.request.MprApprovalRequest;
import com.procurement.dto.request.MprDetailRequest;
import com.procurement.dto.request.MprRequest;
import com.procurement.dto.request.MprUpdateRequest;
import com.procurement.dto.responce.*;
import com.procurement.entity.MprDetail;
import com.procurement.entity.MprHeader;
import com.procurement.entity.MprVendorMapping;
import com.procurement.entity.Priority;
import com.procurement.helper.CurrentUser;
import com.procurement.mapper.MprDetailMapper;
import com.procurement.mapper.MprMapper;
import com.procurement.repository.*;
import com.procurement.service.MprRegServices;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MprRegServicesImpl implements MprRegServices {
    private final MprMapper mprMapper;
    private final MprRepository mprRepository;
    @Autowired
    MprDetailRepository mprDetailRepository;
    @Autowired
    private MprDetailMapper mapper;

    @Autowired
    MprVendorMappingRepository mprVendorMappingRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TenderTypeRepository tenderTypeRepository;

    @Autowired
    private MprTypeRepository mprTypeRepository;


    @Override
    @Transactional
    public ResponseEntity<ApiResponse<MprDto>> mprReg(MprRequest request) {
        MprHeader mprHeader = mprMapper.toEntity(request);
        mprHeader.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
        mprRepository.save(mprHeader);
        for (MprDetailRequest detail : request.getMprDetailRequests()) {
            MprDetail mprDetail = new MprDetail();
            mprDetail.setMprHeader(mprRepository.findById(mprHeader.getMprId())
                    .orElseThrow(() -> new RuntimeException("mpr header Id  not found")));
            mprDetail.setSlNo(detail.getSlNo());
            mprDetail.setItemCode(detail.getItemCode());
            mprDetail.setItemName(detail.getItemName());
            mprDetail.setUom(detail.getUom());
            mprDetail.setSpecification(detail.getSpecification());
            mprDetail.setRequestedQty(detail.getRequestedQty());
            mprDetail.setEstimatedRate(detail.getEstimatedRate());
            mprDetail.setEstimatedValue(detail.getEstimatedValue());
            mprDetail.setStockAvailable(detail.getStockAvailable());
            mprDetail.setAvgMonthlyConsumption(detail.getAvgMonthlyConsumption());
            mprDetail.setLastPurchaseInfo(detail.getLastPurchaseInfo());
            mprDetail.setRemarks(detail.getRemarks());
            mprDetail.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
            mprDetail.setCreatedAt(java.time.LocalDateTime.now());
            mprDetail.setStatus("n");
            mprDetailRepository.save(mprDetail);
            // 3. 🔥 Vendor Mapping Logic
            if (detail.getVendorIds() != null && !detail.getVendorIds().isEmpty()) {
                List<MprVendorMapping> mappings = detail.getVendorIds()
                        .stream()
                        .map(vendorId -> {
                            MprVendorMapping mapping = new MprVendorMapping();
                            mapping.setMprId(mprHeader.getMprId());
                            mapping.setMprDetailId(mprDetail.getMprDetailId());
                            mapping.setVendorId(vendorId);
                            return mapping;
                        })
                        .collect(Collectors.toList());
                mprVendorMappingRepository.saveAll(mappings);
            }
        }
        return ResponseUtil.success(mprMapper.toDto(mprHeader), "MPR registered successfully");
    }
    @Override
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprs(String status) {
        List<MprDetail> details = mprDetailRepository.findByStatusWithHeader(status);
        Map<Long, MprResponse> responseMap = new LinkedHashMap<>();
        for (MprDetail detail : details) {
            MprHeader header = detail.getMprHeader();
            Long mprId = header.getMprId();
            MprResponse response;
            // ✅ Agar header already map me hai to reuse karo
            if (responseMap.containsKey(mprId)) {
                response = responseMap.get(mprId);
            } else {
                // ❗ New header create karo
                response = new MprResponse();
                response.setMprId(header.getMprId().toString());
                response.setMprNo(header.getMprNo());
                response.setMprDate(header.getMprDate());
                response.setDepartmentId(header.getDepartment().getDepartmentId());
                response.setProjectName(header.getProjectName());
                response.setMprTypeId(header.getMprType().getTypeId());
                response.setTenderTypeId(header.getTenderType().getTenderTypeId());
                response.setPriority(header.getPriority().toString());
                response.setRequiredByDate(header.getRequiredByDate());
                response.setDeliverySchedule(header.getDeliverySchedule());
                response.setDurationDays(header.getDurationDays());
                response.setSpecialNotes(header.getSpecialNotes());
                response.setJustification(header.getJustification());

                response.setMprDetailResponnces(new ArrayList<>());

                responseMap.put(mprId, response);
            }
            // ✅ Detail add karo
            MprDetailResponnce d = new MprDetailResponnce();
            d.setMprDetailId(detail.getMprDetailId());
            d.setSlNo(detail.getSlNo());
            d.setItemCode(detail.getItemCode());
            d.setItemName(detail.getItemName());
            d.setUom(detail.getUom());
            d.setSpecification(detail.getSpecification());
            d.setRequestedQty(detail.getRequestedQty());
            d.setEstimatedRate(detail.getEstimatedRate());
            d.setEstimatedValue(detail.getEstimatedValue());
            d.setStockAvailable(detail.getStockAvailable());
            d.setAvgMonthlyConsumption(detail.getAvgMonthlyConsumption());
            d.setLastPurchaseInfo(detail.getLastPurchaseInfo());
            d.setRemarks(detail.getRemarks());
            d.setStatus(detail.getStatus());
            response.getMprDetailResponnces().add(d);
        }
        if (responseMap.isEmpty()) {
            return ResponseUtil.notFound("No Record Found");
        }
        return ResponseUtil.success(new ArrayList<>(responseMap.values()));
    }
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<MprDetailDTO>> mprApproval(MprApprovalRequest request) {
        List<MprDetail> details = mprDetailRepository.findByMprHeaderMprId(request.getMprId());
        if (details.isEmpty()) {
            throw new RuntimeException("No MPR details found for given MPR ID");
        }
        for (MprDetail detail : details) {
            mapper.updateApproval(detail, request);
        }
        mprDetailRepository.saveAll(details);
        return ResponseUtil.success(null, "MPR registered successfully");
    }
//    @Transactional
//    @Override
//    public ResponseEntity<ApiResponse<String>> updateMpr(MprUpdateRequest request) {
//        // ✅ 1. Fetch Header
//        MprHeader header = mprRepository.findById(request.getMprId())
//                .orElseThrow(() -> new RuntimeException("MPR not found"));
//        // ✅ 2. Update Header fields
//        header.setMprNo(request.getMprNo());
//        header.setMprDate(request.getMprDate());
//        header.setProjectName(request.getProjectName());
//        header.setPriority(Priority.valueOf(request.getPriority()));
//        header.setRequiredByDate(request.getRequiredByDate());
//        header.setDeliverySchedule(request.getDeliverySchedule());
//        header.setDurationDays(request.getDurationDays());
//        header.setSpecialNotes(request.getSpecialNotes());
//        header.setJustification(request.getJustification());
//       // header.setStatus(request.getStatus());
//        header.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
//        // Foreign keys (assuming fetch from repo)
//        header.setDepartment(departmentRepository.findById(request.getDepartmentId().intValue()).orElse(null));
//        header.setMprType(mprTypeRepository.findById(request.getMprTypeId()).orElse(null));
//        header.setTenderType(tenderTypeRepository.findById(request.getTenderTypeId()).orElse(null));
//        mprRepository.save(header);
//
//        // ✅ 3. DELETE Details
//        if (request.getDeleteDetailIds() != null && !request.getDeleteDetailIds().isEmpty()) {
//            mprDetailRepository.deleteAllById(request.getDeleteDetailIds());
//        }
//        // ✅ 4. INSERT / UPDATE Details
//        for (MprDetailDTO dto : request.getDetails()) {
//            if (dto.getMprDetailId() == null) {
//                // ➕ INSERT
//                MprDetail newDetail = new MprDetail();
//                mapper.mapDtoToEntity(dto, newDetail);
//                newDetail.setMprHeader(header);
//                mprDetailRepository.save(newDetail);
//            } else {
//                // 🔄 UPDATE
//                MprDetail existing = mprDetailRepository.findById(dto.getMprDetailId())
//                        .orElseThrow(() -> new RuntimeException("Detail not found: " + dto.getMprDetailId()));
//                mapper.mapDtoToEntity(dto, existing);
//                mprDetailRepository.save(existing);
//            }
//        }
//        return ResponseUtil.success("MPR Updated Successfully");
//    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<String>> updateMpr(MprUpdateRequest request) {

        // ✅ 1. FETCH HEADER
        MprHeader header = mprRepository.findById(request.getMprId())
                .orElseThrow(() -> new RuntimeException("MPR not found"));

        // ✅ 2. UPDATE HEADER
        header.setMprNo(request.getMprNo());
        header.setMprDate(request.getMprDate());
        header.setProjectName(request.getProjectName());
        header.setPriority(Priority.valueOf(request.getPriority()));
        header.setRequiredByDate(request.getRequiredByDate());
        header.setDeliverySchedule(request.getDeliverySchedule());
        header.setDurationDays(request.getDurationDays());
        header.setSpecialNotes(request.getSpecialNotes());
        header.setJustification(request.getJustification());
        header.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);

        header.setDepartment(
                departmentRepository.findById(request.getDepartmentId().intValue()).orElse(null)
        );
        header.setMprType(
                mprTypeRepository.findById(request.getMprTypeId()).orElse(null)
        );
        header.setTenderType(
                tenderTypeRepository.findById(request.getTenderTypeId()).orElse(null)
        );
        mprRepository.save(header);

        // ✅ 3. DELETE DETAILS + THEIR VENDOR MAPPINGS
        if (request.getDeleteDetailIds() != null && !request.getDeleteDetailIds().isEmpty()) {

            for (Long detailId : request.getDeleteDetailIds()) {
                //  delete vendor mapping first
                mprVendorMappingRepository.deleteByMprDetailMprDetailId(detailId);
            }
            //  delete details
            mprDetailRepository.deleteAllById(request.getDeleteDetailIds());
        }
        // ✅ 4. INSERT / UPDATE DETAILS + VENDOR MAPPING
        for (MprDetailDTO dto : request.getDetails()) {
            MprDetail detail;
            if (dto.getMprDetailId() == null) {
                // ➕ INSERT DETAIL
                detail = new MprDetail();
                mapper.mapDtoToEntity(dto, detail);
                detail.setMprHeader(header);
                mprDetailRepository.save(detail);

            } else {
                //  UPDATE DETAIL
                detail = mprDetailRepository.findById(dto.getMprDetailId())
                        .orElseThrow(() ->
                                new RuntimeException("Detail not found: " + dto.getMprDetailId())
                        );
                mapper.mapDtoToEntity(dto, detail);
                mprDetailRepository.save(detail);
                //  OLD VENDOR MAPPING DELETE
                mprVendorMappingRepository.deleteByMprDetailMprDetailId(detail.getMprDetailId());
            }
            // ✅ INSERT NEW VENDOR MAPPING
            if (dto.getVendorIds() != null && !dto.getVendorIds().isEmpty()) {
                List<MprVendorMapping> mappings = new ArrayList<>();
                for (Long vendorId : dto.getVendorIds()) {
                    MprVendorMapping mapping = new MprVendorMapping();
                    mapping.setMprId(header.getMprId());
                    mapping.setMprDetailId(detail.getMprDetailId());
                    mapping.setVendorId(vendorId);
                    mappings.add(mapping);
                }
                mprVendorMappingRepository.saveAll(mappings);
            }
        }
        return ResponseUtil.success("MPR Updated Successfully");
    }
}





