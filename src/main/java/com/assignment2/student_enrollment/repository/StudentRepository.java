package com.assignment2.student_enrollment.repository;
import com.assignment2.student_enrollment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student,Integer> {
    Student findByUserName(String username);
}
