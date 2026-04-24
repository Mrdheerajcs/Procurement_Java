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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class MprRegServicesImpl implements MprRegServices {
    private final MprMapper mprMapper;
    private final MprRepository mprRepository;
    @Autowired
    MprDetailRepository mprDetailRepository;
    @Autowired
    private MprDetailMapper mapper;

    @Value("${app.upload.base-dir:C:/uploads}")
    private String baseDir;

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
        log.info("Registering MPR with total value: {}", request.getTotalValue());

        MprHeader mprHeader = mprMapper.toEntity(request);
        mprHeader.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
        mprHeader.setTotalValue(request.getTotalValue());  // ✅ Set total value

        MprHeader savedHeader = mprRepository.save(mprHeader);

        for (MprDetailRequest detail : request.getMprDetailRequests()) {
            MprDetail mprDetail = new MprDetail();
            mprDetail.setMprHeader(savedHeader);
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

            if (detail.getVendorIds() != null && !detail.getVendorIds().isEmpty()) {
                List<MprVendorMapping> mappings = detail.getVendorIds()
                        .stream()
                        .map(vendorId -> {
                            MprVendorMapping mapping = new MprVendorMapping();
                            mapping.setMprId(savedHeader.getMprId());
                            mapping.setMprDetailId(mprDetail.getMprDetailId());
                            mapping.setVendorId(vendorId);
                            return mapping;
                        })
                        .collect(Collectors.toList());
                mprVendorMappingRepository.saveAll(mappings);
            }
        }

        return ResponseUtil.success(mprMapper.toDto(savedHeader), "MPR registered successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprs(String status) {
        List<MprDetail> details = mprDetailRepository.findByStatusWithHeader(status);
        Map<Long, MprResponse> responseMap = new LinkedHashMap<>();
        List<Long> detailIds = details.stream()
                .map(MprDetail::getMprDetailId)
                .collect(Collectors.toList());
        List<MprVendorMapping> mappings = mprVendorMappingRepository.findByMprDetailIds(detailIds);
        Map<Long, List<Long>> vendorMap = mappings.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getMprDetailId(),
                        Collectors.mapping(MprVendorMapping::getVendorId, Collectors.toList())));
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
                response.setDocumentPath(header.getDocumentPath());
                response.setTotalValue(header.getTotalValue());
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

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<String>> updateMpr(MprUpdateRequest request) {
        log.info("Updating MPR with ID: {}", request.getMprId());

        // 1. FETCH HEADER
        MprHeader header = mprRepository.findById(request.getMprId())
                .orElseThrow(() -> new RuntimeException("MPR not found"));

        // 2. UPDATE HEADER
        header.setMprNo(request.getMprNo());
        header.setMprDate(request.getMprDate());
        header.setProjectName(request.getProjectName());
        if (request.getPriority() != null) {
            header.setPriority(Priority.valueOf(request.getPriority()));
        }
        header.setRequiredByDate(request.getRequiredByDate());
        header.setDeliverySchedule(request.getDeliverySchedule());
        header.setDurationDays(request.getDurationDays());
        header.setSpecialNotes(request.getSpecialNotes());
        header.setJustification(request.getJustification());
        if (request.getTotalValue() != null) {
            header.setTotalValue(request.getTotalValue());
        }
        header.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);

        if (request.getDepartmentId() != null) {
            header.setDepartment(departmentRepository.findById(request.getDepartmentId().intValue()).orElse(null));
        }
        if (request.getMprTypeId() != null) {
            header.setMprType(mprTypeRepository.findById(request.getMprTypeId()).orElse(null));
        }
        if (request.getTenderTypeId() != null) {
            header.setTenderType(tenderTypeRepository.findById(request.getTenderTypeId()).orElse(null));
        }
        mprRepository.save(header);

        // 3. DELETE REMOVED DETAILS
        if (request.getDeleteDetailIds() != null && !request.getDeleteDetailIds().isEmpty()) {
            for (Long detailId : request.getDeleteDetailIds()) {
                mprVendorMappingRepository.deleteByMprDetailId(detailId);
            }
            mprDetailRepository.deleteAllById(request.getDeleteDetailIds());
            log.info("Deleted details with IDs: {}", request.getDeleteDetailIds());
        }

        // 4. UPDATE OR CREATE DETAILS - ✅ USING MprDetailUpdateDTO
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (MprDetailUpdateDTO dto : request.getDetails()) {
                MprDetail detail;

                if (dto.getMprDetailId() == null) {
                    // CREATE NEW DETAIL
                    detail = new MprDetail();
                    detail.setMprHeader(header);
                    detail.setSlNo(dto.getSlNo());
                    detail.setItemCode(dto.getItemCode());
                    detail.setItemName(dto.getItemName());
                    detail.setUom(dto.getUom());
                    detail.setSpecification(dto.getSpecificationn());  // Map from specificationn
                    detail.setRequestedQty(dto.getRequestedQty());
                    detail.setEstimatedRate(dto.getEstimatedRate());
                    detail.setEstimatedValue(dto.getEstimatedValue());
                    detail.setStockAvailable(dto.getStockAvailable());
                    detail.setAvgMonthlyConsumption(dto.getAvgMonthlyConsumption());
                    detail.setLastPurchaseInfo(dto.getLastPurchaseInfo());
                    detail.setRemarks(dto.getRemarks());
                    detail.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
                    detail.setCreatedAt(java.time.LocalDateTime.now());
                    detail.setStatus("n");
                    detail = mprDetailRepository.save(detail);
                    log.info("Created new detail for MPR: {}", header.getMprId());
                } else {
                    // UPDATE EXISTING DETAIL
                    detail = mprDetailRepository.findById(dto.getMprDetailId())
                            .orElseThrow(() -> new RuntimeException("Detail not found: " + dto.getMprDetailId()));

                    detail.setSlNo(dto.getSlNo());
                    detail.setItemCode(dto.getItemCode());
                    detail.setItemName(dto.getItemName());
                    detail.setUom(dto.getUom());
                    detail.setSpecification(dto.getSpecificationn());  // Map from specificationn
                    detail.setRequestedQty(dto.getRequestedQty());
                    detail.setEstimatedRate(dto.getEstimatedRate());
                    detail.setEstimatedValue(dto.getEstimatedValue());
                    detail.setStockAvailable(dto.getStockAvailable());
                    detail.setAvgMonthlyConsumption(dto.getAvgMonthlyConsumption());
                    detail.setLastPurchaseInfo(dto.getLastPurchaseInfo());
                    detail.setRemarks(dto.getRemarks());
                    detail.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
                    detail = mprDetailRepository.save(detail);
                    log.info("Updated detail ID: {} for MPR: {}", dto.getMprDetailId(), header.getMprId());

                    // Delete existing vendor mappings for this detail
                    mprVendorMappingRepository.deleteByMprDetailId(detail.getMprDetailId());
                }

                // 5. SAVE VENDOR MAPPINGS
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
                    log.info("Saved {} vendor mappings for detail ID: {}", mappings.size(), detail.getMprDetailId());
                }
            }
        }

        return ResponseUtil.success("MPR Updated Successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> publishTender(
            TenderRequest request,
            MultipartFile nitDoc,
            MultipartFile boqDoc,
            MultipartFile techDoc,
            List<MultipartFile> otherDocs
    ) throws IOException {

        log.info("Publishing tender for MPR ID: {}", request.getMprId());

        // ================= HEADER =================
        TenderHeader header = new TenderHeader();
        header.setMprId(request.getMprId());

        String tenderNo = request.getTenderNo();
        if (tenderNo == null || tenderNo.isEmpty()) {
            tenderNo = "TND/" + System.currentTimeMillis();
        }
        header.setTenderNo(tenderNo);

        header.setTenderTitle(request.getTenderTitle());
        header.setTenderType(request.getTenderType());
        header.setDepartment(request.getDepartment());
        header.setProjectName(request.getProjectName());
        header.setPriority(request.getPriority());

        header.setBidType(request.getBidType());
        header.setBoqType(request.getBoqType());
        header.setEstimatedValue(request.getEstimatedValue());
        header.setTenderFee(request.getTenderFee());
        header.setBidValidity(request.getBidValidity());

        header.setPublishDate(request.getPublishDate());
        header.setBidStartDate(request.getPublishDate());
        header.setBidEndDate(request.getClosingDate());
        header.setBidSubmissionEndTime(request.getBidSubmissionEndTime());

        header.setPreBidMeetingDate(request.getPreBidDate());
        header.setTechBidOpenDate(request.getTechBidOpenDate());
        header.setFinBidOpenDate(request.getFinBidOpenDate());
        header.setBidOpeningDate(request.getClosingDate());

        header.setEmdAmount(request.getEmdAmount());
        header.setTenderDescription(request.getDescription());

        header.setTenderStatus("PENDING_APPROVAL");
        header.setStatus("PENDING_APPROVAL");

        String currentUser = CurrentUser.getCurrentUserOrThrow().getUsername();
        header.setAuditFields(currentUser, true);

        TenderHeader saved = headerRepo.save(header);
        Long tenderId = saved.getTenderId();

        log.info("Tender saved with ID: {}", tenderId);

        // ================= FILE STORAGE =================
        String tenderDirPath = baseDir + "/tenders/" + tenderId;
        File tenderDir = new File(tenderDirPath);
        if (!tenderDir.exists()) {
            tenderDir.mkdirs();
        }

        // Save categorized files
        saveFile(nitDoc, "NIT", tenderId, tenderDir);
        saveFile(boqDoc, "BOQ", tenderId, tenderDir);
        saveFile(techDoc, "TECH", tenderId, tenderDir);

        if (otherDocs != null && !otherDocs.isEmpty()) {
            for (MultipartFile file : otherDocs) {
                saveFile(file, "OTHER", tenderId, tenderDir);
            }
        }

        return ResponseUtil.success("Tender created successfully and submitted for approval");
    }



    private void saveFile(MultipartFile file, String category, Long tenderId, File uploadDir) throws IOException {

        if (file == null || file.isEmpty()) return;

        String originalName = file.getOriginalFilename();

        String fileName = System.currentTimeMillis() + "_" +
                originalName.replaceAll("\\s+", "_");

        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);

        TenderDocument doc = new TenderDocument();
        doc.setTenderId(tenderId);
        doc.setFileName(originalName);
        doc.setFilePath(dest.getAbsolutePath());
        doc.setFileType(file.getContentType());
        doc.setDocCategory(category); // ⭐ IMPORTANT

        docRepo.save(doc);

        log.info("Saved {} document: {}", category, originalName);
    }

    @Override
    @Transactional
    public void updateDocumentPath(Long mprId, String documentPath) {
        log.info("Updating document path for MPR: {}", mprId);
        MprHeader header = mprRepository.findById(mprId)
                .orElseThrow(() -> new RuntimeException("MPR not found with id: " + mprId));
        header.setDocumentPath(documentPath);
        mprRepository.save(header);
    }


    // Add this method to MprRegServicesImpl.java

    @Override
    public ResponseEntity<ApiResponse<List<MprDocumentDto>>> getMprDocuments(Long mprId) {
        log.info("Fetching documents for MPR: {}", mprId);

        MprHeader header = mprRepository.findById(mprId)
                .orElseThrow(() -> new RuntimeException("MPR not found"));

        String documentPath = header.getDocumentPath();
        List<MprDocumentDto> documents = new ArrayList<>();

        if (documentPath != null && !documentPath.isEmpty()) {
            String[] paths = documentPath.split(",");
            for (String path : paths) {
                File file = new File(path.trim());
                if (file.exists()) {
                    documents.add(MprDocumentDto.builder()
                            .fileName(file.getName())
                            .filePath(file.getAbsolutePath())
                            .fileType(getFileExtension(file.getName()))
                            .fileSize(file.length())
                            .build());
                }
            }
        }

        return ResponseUtil.success(documents, "Documents retrieved successfully");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "unknown";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "unknown";
    }

}





