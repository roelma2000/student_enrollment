package com.assignment2.student_enrollment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity  //for creating the table
@Table(name = "tblstudent", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //generating auto inc
    @Column(name = "id")
    private int studentId;
    @NotBlank(message = "Username should not be blank.")
    private String userName;
    @NotBlank(message = "Password should not be blank.")
    private String password;
    @NotBlank(message = "Firstname should not be blank.")
    private String firstname;
    @NotBlank(message = "Lastname should not be blank.")
    private String lastname;
    @NotBlank(message = "Address should not be blank.")
    private String address;
    @NotBlank(message = "City should not be blank.")
    private String city;
    @NotBlank(message = "PostalCode should not be blank.")
    @Size(max = 7, message = "PostalCode should not exceed 7 characters.")
    private String postalCode;

}
