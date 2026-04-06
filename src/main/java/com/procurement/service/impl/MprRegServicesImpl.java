package com.procurement.service.impl;
import com.procurement.dto.request.MprApprovalRequest;
import com.procurement.dto.request.MprDetailRequest;
import com.procurement.dto.request.MprRequest;
import com.procurement.dto.responce.*;
import com.procurement.entity.MprDetail;
import com.procurement.entity.MprHeader;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class MprRegServicesImpl implements MprRegServices {
    private final MprMapper mprMapper;
    private final MprRepository mprRepository;
    @Autowired
    MprDetailRepository mprDetailRepository;
    @Autowired
    private MprDetailMapper mapper;

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
            mprDetailRepository.save(mprDetail);
        }
        return ResponseUtil.success(mprMapper.toDto(mprHeader), "MPR registered successfully");
    }
    @Override
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprs() {
        List<MprHeader> headers = mprRepository.findAll();
        List<MprResponse> responseList = new ArrayList<>();
        for (MprHeader header : headers) {
            MprResponse response = new MprResponse();
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
            // 🔁 Details Mapping
            List<MprDetail> detailList = mprDetailRepository.findByMprHeader(header);
            List<MprDetailResponnce> responnces=new ArrayList<>();
            for (MprDetail detail :  detailList) {  //mprDetailRequests
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
                responnces.add(d);
            }
            response.setMprDetailResponnces(responnces);
            responseList.add(response);
        }
        return ResponseUtil.success(responseList);
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
}





