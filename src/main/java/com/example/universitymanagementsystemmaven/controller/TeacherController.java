
package com.example.universitymanagementsystemmaven.controller;

import com.example.universitymanagementsystemmaven.dto.TeacherCreateRequest;
import com.example.universitymanagementsystemmaven.dto.TeacherUpdateRequest;
import com.example.universitymanagementsystemmaven.dto.TeacherResponse;
import com.example.universitymanagementsystemmaven.service.TeacherManagementService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherManagementService teacherService;

    public TeacherController(
            TeacherManagementService teacherService) {

        this.teacherService = teacherService;
    }

    // CREATE TEACHER - ADMIN ONLY
    @PostMapping
    public ResponseEntity<TeacherResponse> createTeacher(
            @Valid @RequestBody TeacherCreateRequest request) {

        TeacherResponse response = teacherService.create(
                request.fullName(),
                request.email(),
                request.department(),
                request.designation(),
                request.username(),
                request.password()
        );

        return ResponseEntity
                .created(URI.create(
                        "/api/teachers/" + response.id()))
                .body(response);
    }

    // GET ALL TEACHERS - ADMIN ONLY
    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {

        return ResponseEntity.ok(
                teacherService.findAll()
        );
    }

    // GET LOGGED-IN TEACHER PROFILE
    // Used by the Teacher Portal UI
    @GetMapping("/me")
    public ResponseEntity<TeacherResponse> getMyProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                teacherService.findMyProfile(authentication)
        );
    }

    // GET TEACHER BY ID
    // ADMIN: Can view any teacher
    // TEACHER: Can view own profile only
    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getTeacherById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                teacherService.findById(id, authentication)
        );
    }

    // UPDATE TEACHER
    // ADMIN: Can update any teacher
    // TEACHER: Can update own profile only
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequest request,
            Authentication authentication) {

        TeacherResponse response = teacherService.update(
                id,
                request.fullName(),
                request.email(),
                request.department(),
                request.designation(),
                authentication
        );

        return ResponseEntity.ok(response);
    }

    // DELETE TEACHER - ADMIN ONLY
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(
            @PathVariable Long id) {

        teacherService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
