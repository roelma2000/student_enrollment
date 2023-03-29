package com.assignment2.student_enrollment.controller;
import com.assignment2.student_enrollment.model.Student;
import com.assignment2.student_enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class StudentController {
    @Autowired
    private StudentRepository studentRepository;

    /**
     * Code for index page - registration login
     */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @RequestMapping({"/", "/login"})
    public String getLoginPage(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("authenticationError") != null) {
            model.addAttribute("authenticationError", request.getSession().getAttribute("authenticationError"));
            request.getSession().removeAttribute("authenticationError");
        }
        return "index";
    }


    /**
     * Code for enrollment page
     */
    @GetMapping("/enrollment")
    public String enrollmentPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Student currentUser = studentRepository.findByUserName(auth.getName());
        model.addAttribute("currentUser", currentUser);
        // Add any other necessary attributes or data to the model
        return "enrollment";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("student", new Student());
        return "register";
    }

    @PostMapping("/register")
    public String registerStudent(@Valid Student student, BindingResult bindingResult, Model model){
        //Username validation
        if(student.getUserName().isBlank()){
            bindingResult.addError(new FieldError("student","userName","Username should not be blank."));
        }
        //Password validation
        if(student.getPassword().isBlank()){
            bindingResult.addError(new FieldError("student","password","Password should not be blank."));
        }
        //firstname
        if(student.getFirstname().isBlank()){
            bindingResult.addError(new FieldError("student","firstname","Firstname should not be blank."));
        }
        //lastname
        if(student.getLastname().isBlank()){
            bindingResult.addError(new FieldError("student","lastname","Lastname should not be blank."));
        }
        //address
        if(student.getAddress().isBlank()){
            bindingResult.addError(new FieldError("student","address","Address should not be blank."));
        }
        //city
        if(student.getCity().isBlank()){
            bindingResult.addError(new FieldError("student","city","City should not be blank."));
        }
        // postalCode
        if(student.getPostalCode().isBlank() || student.getPostalCode().length() > 7){
            bindingResult.addError(new FieldError("student","postalCode","postalCode should not be blank."));
        }

        if(bindingResult.hasErrors()){
            return "register";
        }
        studentRepository.save(student);
        model.addAttribute("students", studentRepository.findAll());
        return "redirect:/login";
    }

}
