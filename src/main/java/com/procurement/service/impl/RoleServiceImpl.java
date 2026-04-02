package com.procurement.service.impl;

import com.procurement.dto.request.RoleRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.RoleDto;
import com.procurement.entity.Role;
import com.procurement.entity.User;
import com.procurement.mapper.RoleMapper;
import com.procurement.repository.RoleRepository;
import com.procurement.repository.UserRepository;
import com.procurement.service.RoleService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final UserRepository userRepo;
    private final RoleRepository repo;

    private final RoleMapper mapper;


    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = repo.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        userRepo.save(user); // changes effective immediately
    }


    @Override
    @Transactional
    public ResponseEntity<ApiResponse<RoleDto>> create(RoleRequest request) {

        Role role = mapper.toEntity(request);
        repo.save(role);

        return ResponseUtil.success(mapper.toDto(role), "Role created");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<RoleDto>> update(RoleRequest request) {

        Role role = repo.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        mapper.updateEntity(role, request);

        return ResponseUtil.success(mapper.toDto(role), "Role updated");
    }

    @Override
    public ResponseEntity<ApiResponse<RoleDto>> getById(Long id) {

        Role role = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        return ResponseUtil.success(mapper.toDto(role), "Success");
    }

    @Override
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAll() {

        List<RoleDto> list = repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();

        return ResponseUtil.success(list, "Success");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status) {

        Role role = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setStatus(status);

        return ResponseUtil.success("Status updated", "Success");
    }
}