package com.classpulse.controller;

import com.classpulse.model.*;
import com.classpulse.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/classes/{classId}/students")
public class StudentController {

    private final StudentService studentService;
    private final ClassService classService;
    private final TeacherService teacherService;

    public StudentController(StudentService studentService, ClassService classService, TeacherService teacherService) {
        this.studentService = studentService;
        this.classService = classService;
        this.teacherService = teacherService;
    }

    @PostMapping("/add")
    public String addStudent(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long classId,
                             @Valid @ModelAttribute("newStudent") Student student,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        SchoolClass schoolClass = classService.getClassByIdAndTeacher(classId, teacher.getId());

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("studentError", "Student name is required.");
            return "redirect:/classes/" + classId;
        }

        studentService.addStudent(student, schoolClass);
        redirectAttributes.addFlashAttribute("success", "Student added successfully.");
        return "redirect:/classes/" + classId;
    }

    @PostMapping("/{studentId}/delete")
    public String deleteStudent(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long classId,
                                @PathVariable Long studentId,
                                RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        classService.getClassByIdAndTeacher(classId, teacher.getId()); // ownership check
        studentService.deleteStudent(studentId);
        redirectAttributes.addFlashAttribute("success", "Student removed.");
        return "redirect:/classes/" + classId;
    }
}
