package com.procurement.service.impl;
import com.procurement.dto.request.MprRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprDto;
import com.procurement.entity.MprHeader;
import com.procurement.helper.CurrentUser;
import com.procurement.mapper.MprMapper;
import com.procurement.repository.MprRepository;
import com.procurement.service.MprRegServices;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class MprRegServicesImpl implements MprRegServices {
    private final MprMapper mprMapper;
    private final MprRepository mprRepository;
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<MprDto>> mprReg(MprRequest request) {
        MprHeader mprHeader = mprMapper.toEntity(request);
        mprHeader.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
        mprRepository.save(mprHeader);
        return ResponseUtil.success(mprMapper.toDto(mprHeader), "MPR registered successfully");
    }
}
