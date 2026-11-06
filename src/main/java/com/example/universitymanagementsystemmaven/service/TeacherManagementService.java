
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.dto.TeacherResponse;
import com.example.universitymanagementsystemmaven.entity.Teacher;
import com.example.universitymanagementsystemmaven.repository.TeacherRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeacherManagementService {

    private final TeacherService teacherService;
    private final TeacherRepository teacherRepository;

    public TeacherManagementService(
            TeacherService teacherService,
            TeacherRepository teacherRepository) {

        this.teacherService = teacherService;
        this.teacherRepository = teacherRepository;
    }

    // CREATE TEACHER - ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TeacherResponse create(
            String fullName,
            String email,
            String department,
            String designation,
            String username,
            String password) {

        Teacher teacher = teacherService.createTeacher(
                fullName,
                email,
                department,
                designation,
                username,
                password
        );

        return TeacherResponse.fromEntity(teacher);
    }

    // GET ALL TEACHERS - ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {

        return teacherService.getAllTeachers()
                .stream()
                .map(TeacherResponse::fromEntity)
                .toList();
    }



    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Transactional(readOnly = true)
    public TeacherResponse findById(
            Long id,
            Authentication authentication) {

        Teacher teacher = teacherService.getTeacherById(id);

        checkOwnership(teacher, authentication);

        return TeacherResponse.fromEntity(teacher);
    }

    // GET CURRENTLY LOGGED-IN TEACHER PROFILE
    // Used by the Teacher Portal UI
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional(readOnly = true)
    public TeacherResponse findMyProfile(
            Authentication authentication) {

        Long accountId = getAccountId(authentication);

        Teacher teacher = teacherRepository
                .findByUserAccount_Id(accountId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Teacher profile not found"
                        )
                );

        return TeacherResponse.fromEntity(teacher);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Transactional
    public TeacherResponse update(
            Long id,
            String fullName,
            String email,
            String department,
            String designation,
            Authentication authentication) {

        Teacher teacher = teacherService.getTeacherById(id);

        checkOwnership(teacher, authentication);

        Teacher updated = teacherService.updateTeacher(
                id,
                fullName,
                email,
                department,
                designation
        );

        return TeacherResponse.fromEntity(updated);
    }

    // DELETE TEACHER - ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {

        teacherService.deleteTeacher(id);
    }


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


    private void checkOwnership(
            Teacher teacher,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }


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


        Long accountId = getAccountId(authentication);


        Long ownerAccountId =
                teacher.getUserAccount().getId();

        if (!ownerAccountId.equals(accountId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access your own teacher record"
            );
        }
    }
}
