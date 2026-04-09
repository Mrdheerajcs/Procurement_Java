package com.procurement.service.impl;
import com.procurement.dto.request.*;
import com.procurement.dto.responce.*;
import com.procurement.entity.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
   @Autowired
    VendorRepository  vendorRepository;
   @Autowired
    TenderHeaderRepository headerRepo;
   @Autowired
    TenderDocumentRepository docRepo;

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
        // ✅ 1. Sab detail IDs collect karo
        List<Long> detailIds = details.stream()
                .map(MprDetail::getMprDetailId)
                .collect(Collectors.toList());
        // ✅ 2. Vendor mappings fetch karo
        List<MprVendorMapping> mappings = mprVendorMappingRepository.findByMprDetailIds(detailIds);
        // ✅ 3. Group by mprDetailId
        Map<Long, List<Long>> vendorMap = mappings.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getMprDetailId(),
                        Collectors.mapping(MprVendorMapping::getVendorId, Collectors.toList())));
        // ✅ 4. Existing logic
        for (MprDetail detail : details) {
            MprHeader header = detail.getMprHeader();
            Long mprId = header.getMprId();

            MprResponse response;

            if (responseMap.containsKey(mprId)) {
                response = responseMap.get(mprId);
            } else {
                response = new MprResponse();
                response.setMprId(header.getMprId().toString());
                response.setMprNo(header.getMprNo());
                response.setMprDate(header.getMprDate());
                response.setProjectName(header.getProjectName());
                response.setDepartmentId(header.getDepartment().getDepartmentId());
                response.setMprTypeId(header.getMprType().getTypeId());
                response.setTenderTypeId(header.getTenderType().getTenderTypeId());
                response.setDepartmentName(
                        departmentRepository.findById(header.getDepartment().getDepartmentId().intValue()).orElse(null).getDepartmentName());
                response.setMprTypeName(mprTypeRepository.findById(header.getMprType().getTypeId()).orElse(null).getTypeName());
                response.setTenderTypeName(
                        tenderTypeRepository.findById(header.getTenderType().getTenderTypeId()).orElse(null).getTenderName());
                response.setPriority(header.getPriority().toString());
                response.setRequiredByDate(header.getRequiredByDate());
                response.setDeliverySchedule(header.getDeliverySchedule());
                response.setDurationDays(header.getDurationDays());
                response.setSpecialNotes(header.getSpecialNotes());
                response.setJustification(header.getJustification());
                response.setMprDetailResponnces(new ArrayList<>());
                responseMap.put(mprId, response);
            }
            // ✅ Detail mapping
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
            // VendorIds and name add karo
            List<Long> vendorIds = vendorMap.getOrDefault(detail.getMprDetailId(), new ArrayList<>());
            List<VendorDTORes> vendorDTOList = new ArrayList<>();
            for (Long vendorId : vendorIds) {
                VendorDTORes v = new VendorDTORes();
                v.setVendorId(vendorId);
                v.setVendorName(
                        vendorRepository.findById(vendorId).orElse(null).getVendorName());
                vendorDTOList.add(v);
                d.setVendors(vendorDTOList);
            }
            response.getMprDetailResponnces().add(d);
        }
        if (responseMap.isEmpty()) {
            return ResponseUtil.notFound("No Record Found");
        }
        return ResponseUtil.success(new ArrayList<>(responseMap.values()));
    }

    @Override
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprDataByMultiStatus(List<String> statuses) {

        List<MprDetail> details = mprDetailRepository.findByMultiStatusWithHeader(statuses);

        Map<Long, MprResponse> responseMap = new LinkedHashMap<>();

        List<Long> detailIds = details.stream()
                .map(MprDetail::getMprDetailId)
                .collect(Collectors.toList());

        List<MprVendorMapping> mappings = mprVendorMappingRepository.findByMprDetailIds(detailIds);

        Map<Long, List<Long>> vendorMap = mappings.stream()
                .collect(Collectors.groupingBy(
                        MprVendorMapping::getMprDetailId,
                        Collectors.mapping(MprVendorMapping::getVendorId, Collectors.toList())
                ));

        for (MprDetail detail : details) {

            MprHeader header = detail.getMprHeader();
            Long mprId = header.getMprId();

            MprResponse response;

            if (responseMap.containsKey(mprId)) {
                response = responseMap.get(mprId);
            } else {
                response = new MprResponse();

                response.setMprId(header.getMprId().toString());
                response.setMprNo(header.getMprNo());
                response.setMprDate(header.getMprDate());
                response.setProjectName(header.getProjectName());
                response.setDepartmentId(header.getDepartment().getDepartmentId());
                response.setMprTypeId(header.getMprType().getTypeId());
                response.setTenderTypeId(header.getTenderType().getTenderTypeId());

                response.setDepartmentName(
                        departmentRepository.findById(header.getDepartment().getDepartmentId().intValue())
                                .orElse(null).getDepartmentName());

                response.setMprTypeName(
                        mprTypeRepository.findById(header.getMprType().getTypeId())
                                .orElse(null).getTypeName());

                response.setTenderTypeName(
                        tenderTypeRepository.findById(header.getTenderType().getTenderTypeId())
                                .orElse(null).getTenderName());

                response.setPriority(header.getPriority().toString());
                response.setRequiredByDate(header.getRequiredByDate());
                response.setDeliverySchedule(header.getDeliverySchedule());
                response.setDurationDays(header.getDurationDays());
                response.setSpecialNotes(header.getSpecialNotes());
                response.setJustification(header.getJustification());

                response.setMprDetailResponnces(new ArrayList<>());

                responseMap.put(mprId, response);
            }

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

            List<Long> vendorIds = vendorMap.getOrDefault(detail.getMprDetailId(), new ArrayList<>());

            List<VendorDTORes> vendorDTOList = new ArrayList<>();

            for (Long vendorId : vendorIds) {
                VendorDTORes v = new VendorDTORes();
                v.setVendorId(vendorId);

                v.setVendorName(
                        vendorRepository.findById(vendorId)
                                .orElse(null).getVendorName()
                );

                vendorDTOList.add(v);
            }

            d.setVendors(vendorDTOList);

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
        // 1. Fetch all details for header
        List<MprDetail> details = mprDetailRepository
                .findByMprHeaderMprId(request.getMprHeaderId());
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("No MPR details found for given Header ID");
        }
        // 2. Convert list to Map (mprDetailId -> approval object)
        Map<Long, MprApprovalList> approvalMap = request.getMprApprovalLists()
                .stream()
                .collect(Collectors.toMap(
                        MprApprovalList::getMprdetailId,
                        d -> d
                ));
        // 3. Update matching records
        for (MprDetail detail : details) {
            MprApprovalList approval = approvalMap.get(detail.getMprDetailId());
            if (approval != null) {
                mapper.updateApproval(detail, approval);
            }
        }
        // 4. Save all
        mprDetailRepository.saveAll(details);
        // 5. Convert to DTO
        List<MprDetailDTO> updatedList = details.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseUtil.success(null, "Batch MPR approval processed successfully");
    }
//
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
                mprVendorMappingRepository.deleteByMprDetailId(detailId);
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
                mprVendorMappingRepository.deleteByMprDetailId(detail.getMprDetailId());
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
    @Transactional
    @Override
    public ResponseEntity<ApiResponse<String>> publishTender(TenderRequest request, List<MultipartFile> files) throws IOException {

        TenderHeader header = new TenderHeader();
        header.setMprId(request.getMprId());
        header.setTenderTitle(request.getTenderTitle());
        header.setTenderType(request.getTenderType());
        header.setPublishDate(request.getPublishDate());
        header.setClosingDate(request.getClosingDate());
        header.setBidSubmissionEndTime(request.getBidSubmissionEndTime());
        header.setEmdAmount(request.getEmdAmount());
        header.setTenderDescription(request.getTenderDescription());
        header.setStatus("PUBLISHED");
        header.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);

        headerRepo.save(header);

        String uploadDirPath = "C:/uploads/";
        File uploadDir = new File(uploadDirPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        for (MultipartFile file : files) {

            if (file.isEmpty()) continue;

            String fileName = file.getOriginalFilename();

            // Optional cleanup
            fileName = fileName.replaceAll("\\s+", "_");

            File dest = new File(uploadDir, fileName);
            file.transferTo(dest);

            TenderDocument doc = new TenderDocument();
            doc.setTenderId(header.getTenderId());
            doc.setFileName(fileName);
            doc.setFilePath(dest.getAbsolutePath());
            doc.setFileType(file.getContentType());

            docRepo.save(doc);
        }

        return ResponseUtil.success("Tender Published Successfully");
    }
}





