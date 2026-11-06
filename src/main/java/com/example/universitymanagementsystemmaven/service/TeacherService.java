
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.entity.Role;
import com.example.universitymanagementsystemmaven.entity.Teacher;
import com.example.universitymanagementsystemmaven.entity.UserAccount;

import com.example.universitymanagementsystemmaven.repository.TeacherRepository;
import com.example.universitymanagementsystemmaven.repository.UserAccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    public TeacherService(
            TeacherRepository teacherRepository,
            UserAccountRepository userAccountRepository,
            UserAccountService userAccountService) {

        this.teacherRepository = teacherRepository;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    // CREATE TEACHER
    @Transactional
    public Teacher createTeacher(
            String fullName,
            String email,
            String department,
            String designation,
            String username,
            String password) {

        validateProfile(fullName, email, department);

        if (teacherRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teacher email already exists"
            );
        }

        UserAccount account = userAccountService.createAccount(
                username,
                password,
                Role.TEACHER
        );

        Teacher teacher = new Teacher(
                fullName,
                email,
                department,
                designation,
                account
        );

        return teacherRepository.save(teacher);
    }

    // GET ALL TEACHERS
    @Transactional(readOnly = true)
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // GET TEACHER BY ID
    @Transactional(readOnly = true)
    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher not found"
                ));
    }

    // UPDATE TEACHER
    @Transactional
    public Teacher updateTeacher(
            Long id,
            String fullName,
            String email,
            String department,
            String designation) {

        Teacher teacher = getTeacherById(id);

        validateProfile(fullName, email, department);

        if (!teacher.getEmail().equals(email)
                && teacherRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teacher email already exists"
            );
        }

        teacher.setFullName(fullName);
        teacher.setEmail(email);
        teacher.setDepartment(department);
        teacher.setDesignation(designation);

        return teacherRepository.save(teacher);
    }

    // DELETE TEACHER
    @Transactional
    public void deleteTeacher(Long id) {

        Teacher teacher = getTeacherById(id);

        Long accountId = teacher.getUserAccount().getId();

        teacherRepository.delete(teacher);
        teacherRepository.flush();

        userAccountRepository.deleteById(accountId);
    }

    // VALIDATE TEACHER PROFILE
    private void validateProfile(
            String fullName,
            String email,
            String department) {

        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Full name cannot be empty"
            );
        }

        if (email == null || email.isBlank()
                || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email address"
            );
        }

        if (department == null || department.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Department cannot be empty"
            );
        }
    }
}
