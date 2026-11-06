
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.dto.StudentResponse;
import com.example.universitymanagementsystemmaven.entity.Student;
import com.example.universitymanagementsystemmaven.repository.StudentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentManagementService {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public StudentManagementService(
            StudentService studentService,
            StudentRepository studentRepository) {

        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    // CREATE STUDENT - ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudentResponse create(
            String fullName,
            String email,
            String department,
            Integer semester,
            String username,
            String password) {

        Student student = studentService.createStudent(
                fullName,
                email,
                department,
                semester,
                username,
                password
        );

        return StudentResponse.fromEntity(student);
    }

    // GET ALL STUDENTS - ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {

        return studentService.getAllStudents()
                .stream()
                .map(StudentResponse::fromEntity)
                .toList();
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Transactional(readOnly = true)
    public StudentResponse findById(
            Long id,
            Authentication authentication) {

        Student student = studentService.getStudentById(id);

        checkOwnership(student, authentication);

        return StudentResponse.fromEntity(student);
    }

    // GET LOGGED-IN STUDENT PROFILE
    // Used by the Student Portal UI
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public StudentResponse findMyProfile(
            Authentication authentication) {

        Long accountId = getAccountId(authentication);

        Student student = studentRepository
                .findByUserAccount_Id(accountId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Student profile not found"
                        )
                );

        return StudentResponse.fromEntity(student);
    }

    // UPDATE STUDENT
    // ADMIN: Any student
    // STUDENT: Own profile only
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Transactional
    public StudentResponse update(
            Long id,
            String fullName,
            String email,
            String department,
            Integer semester,
            Authentication authentication) {

        Student student = studentService.getStudentById(id);

        checkOwnership(student, authentication);

        Student updated = studentService.updateStudent(
                id,
                fullName,
                email,
                department,
                semester
        );

        return StudentResponse.fromEntity(updated);
    }

    // DELETE STUDENT - ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {

        studentService.deleteStudent(id);
    }

    // GET ACCOUNT ID FROM VERIFIED JWT
    private Long getAccountId(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid authentication principal"
            );
        }

        try {
            return Long.valueOf(jwt.getSubject());

        } catch (NumberFormatException ex) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid account identity"
            );
        }
    }

    // VERIFY STUDENT RECORD OWNERSHIP
    private void checkOwnership(
            Student student,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        // Admin can access all student records
        boolean isAdmin = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN")
                );

        if (isAdmin) {
            return;
        }

        // Get logged-in account ID
        Long accountId = getAccountId(authentication);

        // Get account ID associated with student
        Long ownerAccountId =
                student.getUserAccount().getId();

        // Students can only access their own records
        if (!ownerAccountId.equals(accountId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access your own student record"
            );
        }
    }
}
