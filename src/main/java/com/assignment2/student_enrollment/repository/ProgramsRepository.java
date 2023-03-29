package com.assignment2.student_enrollment.repository;
import com.assignment2.student_enrollment.model.Programs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramsRepository extends JpaRepository<Programs,Integer> {
}
