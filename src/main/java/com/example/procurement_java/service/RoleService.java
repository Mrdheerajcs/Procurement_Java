package com.example.procurement_java.service;

import com.example.procurement_java.entity.Role;
import com.example.procurement_java.entity.User;
import com.example.procurement_java.repository.RoleRepository;
import com.example.procurement_java.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public RoleService(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepo.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        userRepo.save(user); // changes effective immediately
    }
}