package com.assignment2.student_enrollment.model;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity  //for creating the table
@Table(name = "tblstudent", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //generating auto inc
    private int studentId;
    private String userName;
    private String password;
    private String firstname;
    private String lastname;
    private String address;
    private String city;
    private String postalCode;

    public void setPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        this.password = passwordEncoder.encode(password);
    }

}
