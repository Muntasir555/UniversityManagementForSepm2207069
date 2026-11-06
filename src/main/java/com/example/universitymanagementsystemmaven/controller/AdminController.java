
package com.example.universitymanagementsystemmaven.controller;

import com.example.universitymanagementsystemmaven.dto.AdminCreateRequest;
import com.example.universitymanagementsystemmaven.dto.AdminUpdateRequest;
import com.example.universitymanagementsystemmaven.dto.AdminResponse;
import com.example.universitymanagementsystemmaven.service.AdminManagementService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminManagementService adminService;

    public AdminController(
            AdminManagementService adminService) {

        this.adminService = adminService;
    }

    // CREATE ADMIN
    @PostMapping
    public ResponseEntity<AdminResponse> createAdmin(
            @Valid @RequestBody AdminCreateRequest request) {

        AdminResponse response = adminService.create(
                request.fullName(),
                request.email(),
                request.username(),
                request.password()
        );

        URI location = URI.create(
                "/api/admins/" + response.id()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    // GET ALL ADMINS
    @GetMapping
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {

        return ResponseEntity.ok(
                adminService.findAll()
        );
    }

    // GET ADMIN BY ID
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getAdminById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                adminService.findById(id)
        );
    }

    // UPDATE ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateRequest request) {

        AdminResponse response = adminService.update(
                id,
                request.fullName(),
                request.email()
        );

        return ResponseEntity.ok(response);
    }

    // DELETE ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(
            @PathVariable Long id) {

        adminService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
