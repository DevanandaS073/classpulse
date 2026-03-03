package com.classpulse.controller;

import com.classpulse.model.*;
import com.classpulse.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DashboardController {

    private final ClassService classService;
    private final TeacherService teacherService;

    public DashboardController(ClassService classService, TeacherService teacherService) {
        this.classService = classService;
        this.teacherService = teacherService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        List<SchoolClass> classes = classService.getClassesByTeacher(teacher.getId());
        long classCount = classService.getClassCountByTeacher(teacher.getId());

        model.addAttribute("teacher", teacher);
        model.addAttribute("classes", classes);
        model.addAttribute("classCount", classCount);
        model.addAttribute("maxClasses", 3);
        model.addAttribute("canCreateClass", classCount < 3);
        return "dashboard";
    }
}
