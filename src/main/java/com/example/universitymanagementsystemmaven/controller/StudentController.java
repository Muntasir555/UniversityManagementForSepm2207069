
package com.example.universitymanagementsystemmaven.controller;

import com.example.universitymanagementsystemmaven.dto.StudentCreateRequest;
import com.example.universitymanagementsystemmaven.dto.StudentUpdateRequest;
import com.example.universitymanagementsystemmaven.dto.StudentResponse;
import com.example.universitymanagementsystemmaven.service.StudentManagementService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentManagementService studentService;

    public StudentController(
            StudentManagementService studentService) {

        this.studentService = studentService;
    }

    // CREATE STUDENT - ADMIN ONLY
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody StudentCreateRequest request) {

        StudentResponse response = studentService.create(
                request.fullName(),
                request.email(),
                request.department(),
                request.semester(),
                request.username(),
                request.password()
        );

        return ResponseEntity
                .created(URI.create(
                        "/api/students/" + response.id()))
                .body(response);
    }

    // GET ALL STUDENTS - ADMIN ONLY
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {

        return ResponseEntity.ok(
                studentService.findAll()
        );
    }

    // GET LOGGED-IN STUDENT PROFILE
    // Used by Student Portal UI
    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getMyProfile(
            Authentication authentication) {

        return ResponseEntity.ok(
                studentService.findMyProfile(authentication)
        );
    }

    // GET STUDENT BY ID
    // Admin can view any student
    // Student can view only their own profile
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                studentService.findById(id, authentication)
        );
    }

    // UPDATE STUDENT
    // Admin can update any student
    // Student can update only their own profile
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request,
            Authentication authentication) {

        StudentResponse response = studentService.update(
                id,
                request.fullName(),
                request.email(),
                request.department(),
                request.semester(),
                authentication
        );

        return ResponseEntity.ok(response);
    }

    // DELETE STUDENT - ADMIN ONLY
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {

        studentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
