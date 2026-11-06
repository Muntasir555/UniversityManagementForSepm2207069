
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.entity.Role;
import com.example.universitymanagementsystemmaven.entity.Student;
import com.example.universitymanagementsystemmaven.entity.UserAccount;

import com.example.universitymanagementsystemmaven.repository.StudentRepository;
import com.example.universitymanagementsystemmaven.repository.UserAccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    public StudentService(
            StudentRepository studentRepository,
            UserAccountRepository userAccountRepository,
            UserAccountService userAccountService) {

        this.studentRepository = studentRepository;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    // CREATE STUDENT
    @Transactional
    public Student createStudent(
            String fullName,
            String email,
            String department,
            Integer semester,
            String username,
            String password) {

        validateProfile(fullName, email, department, semester);

        if (studentRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student email already exists"
            );
        }

        UserAccount account = userAccountService.createAccount(
                username,
                password,
                Role.STUDENT
        );

        Student student = new Student(
                fullName,
                email,
                department,
                semester,
                account
        );

        return studentRepository.save(student);
    }

    // GET ALL STUDENTS
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // GET STUDENT BY ID
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student not found"
                ));
    }

    // UPDATE STUDENT
    @Transactional
    public Student updateStudent(
            Long id,
            String fullName,
            String email,
            String department,
            Integer semester) {

        Student student = getStudentById(id);

        validateProfile(fullName, email, department, semester);

        if (!student.getEmail().equals(email)
                && studentRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student email already exists"
            );
        }

        student.setFullName(fullName);
        student.setEmail(email);
        student.setDepartment(department);
        student.setSemester(semester);

        return studentRepository.save(student);
    }

    // DELETE STUDENT
    @Transactional
    public void deleteStudent(Long id) {

        Student student = getStudentById(id);

        Long accountId = student.getUserAccount().getId();

        studentRepository.delete(student);
        studentRepository.flush();

        userAccountRepository.deleteById(accountId);
    }

    // VALIDATE STUDENT PROFILE
    private void validateProfile(
            String fullName,
            String email,
            String department,
            Integer semester) {

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

        if (semester == null || semester < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Semester must be a positive number"
            );
        }
    }
}
