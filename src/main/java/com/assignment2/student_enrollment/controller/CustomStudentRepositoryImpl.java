package com.assignment2.student_enrollment.controller;

import com.assignment2.student_enrollment.model.StudentProgramsDTO;

import com.assignment2.student_enrollment.repository.CustomStudentRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CustomStudentRepositoryImpl implements CustomStudentRepository {
    @Override
    public List<StudentProgramsDTO> findEnrolledProgramsByUserId(String userId) {
        return null;
    }
}
