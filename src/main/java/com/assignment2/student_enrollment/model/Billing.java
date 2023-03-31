package com.assignment2.student_enrollment.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import javax.validation.constraints.*;

@Entity  //for creating the table
@Table(name = "tblbilling")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //generating auto inc
    private int id;
    private String receiptno;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "applicationNo", referencedColumnName = "applicationNo")
    private Enrollment enrollment;
    @NotNull(message = "First name is required")
    private String firstname;
    @NotNull(message = "Last name is required")
    private String lastname;
    @NotNull(message = "Address name is required")
    private String address;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email should not be blank")
    private String email;
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    private String phone;
    private String paytype;
    @NotNull(message = "Card name is required")
    @Size(min = 2, max = 50, message = "Card name must be between 2 and 50 characters")
    private String cardname;
    @NotNull(message = "Card number is required")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    private String cardnumber;
    @NotNull(message = "Expiration date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])/[0-9]{4}", message = "Expiration date must be in the format MM/YYYY")
    private String expiration;
    @NotNull(message = "CVV/CVC is required")
    @Size(min = 3, max = 3, message = "CVV/CVC must be 3 digits")
    private String cvv;

    @Column(name = "applicationNo",insertable = false, updatable = false)
    private int applicationNo;
}
