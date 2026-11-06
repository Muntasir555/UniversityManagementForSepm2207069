
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.dto.AdminResponse;
import com.example.universitymanagementsystemmaven.entity.Admin;
import com.example.universitymanagementsystemmaven.repository.AdminRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminManagementService {

    private final AdminService adminService;
    private final AdminRepository adminRepository;

    public AdminManagementService(
            AdminService adminService,
            AdminRepository adminRepository) {

        this.adminService = adminService;
        this.adminRepository = adminRepository;
    }

    // CREATE ADMIN
    @Transactional
    public AdminResponse create(
            String fullName,
            String email,
            String username,
            String password) {

        Admin admin = adminService.createAdmin(
                fullName,
                email,
                username,
                password
        );

        return AdminResponse.fromEntity(admin);
    }

    // GET ALL ADMINS
    @Transactional(readOnly = true)
    public List<AdminResponse> findAll() {

        return adminService.getAllAdmins()
                .stream()
                .map(AdminResponse::fromEntity)
                .toList();
    }

    // GET ADMIN BY ID
    @Transactional(readOnly = true)
    public AdminResponse findById(Long id) {

        Admin admin = adminService.getAdminById(id);

        return AdminResponse.fromEntity(admin);
    }

    // UPDATE ADMIN
    @Transactional
    public AdminResponse update(
            Long id,
            String fullName,
            String email) {

        Admin admin = adminService.updateAdmin(
                id,
                fullName,
                email
        );

        return AdminResponse.fromEntity(admin);
    }

    // DELETE ADMIN
    @Transactional
    public void delete(Long id) {

        // Confirm administrator exists
        adminService.getAdminById(id);

        // Prevent deletion of the only administrator
        if (adminRepository.count() <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete the last administrator"
            );
        }

        adminService.deleteAdmin(id);
    }
}
