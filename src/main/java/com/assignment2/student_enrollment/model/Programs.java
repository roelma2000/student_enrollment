package com.assignment2.student_enrollment.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity  //for creating the table
@Table(name = "tblprograms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Programs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int programCode;
    private String programName;
    private String duration;
    private double fee;
}
