package com.procurement.mapper;

import com.procurement.dto.request.RoleRequest;
import com.procurement.dto.responce.RoleDto;
import com.procurement.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role toEntity(RoleRequest req) {
        Role r = new Role();
        r.setRoleName(req.getRoleName());
        return r;
    }

    public void updateEntity(Role r, RoleRequest req) {
        r.setRoleName(req.getRoleName());
    }

    public RoleDto toDto(Role r) {
        RoleDto dto = new RoleDto();

        dto.setRoleId(r.getRoleId());
        dto.setRoleName(r.getRoleName());
        dto.setStatus(r.getStatus());

        return dto;
    }
}