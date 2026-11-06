
package com.example.universitymanagementsystemmaven.dto;

import com.example.universitymanagementsystemmaven.entity.Student;

public record StudentResponse(
        Long id,
        String fullName,
        String email,
        String department,
        Integer semester,
        String username
) {

    public static StudentResponse fromEntity(Student student) {

        return new StudentResponse(
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                student.getDepartment(),
                student.getSemester(),
                student.getUserAccount().getUsername()
        );
    }
}
