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

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/classes")
public class ClassController {

    private final ClassService classService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final TestService testService;

    public ClassController(ClassService classService, TeacherService teacherService,
                           StudentService studentService, TestService testService) {
        this.classService = classService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.testService = testService;
    }

    @GetMapping("/create")
    public String createClassForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        long count = classService.getClassCountByTeacher(teacher.getId());
        if (count >= 3) {
            return "redirect:/dashboard?classLimitReached=true";
        }
        model.addAttribute("schoolClass", new SchoolClass());
        model.addAttribute("academicYears", AcademicYear.values());
        model.addAttribute("firstYearSubjects", classService.getSubjectsForYear(AcademicYear.FIRST_YEAR));
        model.addAttribute("secondYearSubjects", classService.getSubjectsForYear(AcademicYear.SECOND_YEAR));
        return "create-class";
    }

    @PostMapping("/create")
    public String createClass(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("schoolClass") SchoolClass schoolClass,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        if (result.hasErrors()) {
            model.addAttribute("academicYears", AcademicYear.values());
            model.addAttribute("firstYearSubjects", classService.getSubjectsForYear(AcademicYear.FIRST_YEAR));
            model.addAttribute("secondYearSubjects", classService.getSubjectsForYear(AcademicYear.SECOND_YEAR));
            return "create-class";
        }
        try {
            classService.createClass(schoolClass, teacher);
            redirectAttributes.addFlashAttribute("success", "Class created successfully!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("academicYears", AcademicYear.values());
            model.addAttribute("firstYearSubjects", classService.getSubjectsForYear(AcademicYear.FIRST_YEAR));
            model.addAttribute("secondYearSubjects", classService.getSubjectsForYear(AcademicYear.SECOND_YEAR));
            return "create-class";
        }
    }

    @GetMapping("/{classId}")
    public String classDetails(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long classId, Model model) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        SchoolClass schoolClass = classService.getClassByIdAndTeacher(classId, teacher.getId());
        List<Student> students = studentService.getStudentsByClass(classId);
        List<Test> tests = testService.getTestsByClass(classId);

        model.addAttribute("schoolClass", schoolClass);
        model.addAttribute("students", students);
        model.addAttribute("tests", tests);
        model.addAttribute("newStudent", new Student());
        model.addAttribute("newTest", new Test());
        return "class-details";
    }

    @PostMapping("/{classId}/delete")
    public String deleteClass(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long classId, RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        classService.deleteClass(classId, teacher.getId());
        redirectAttributes.addFlashAttribute("success", "Class deleted.");
        return "redirect:/dashboard";
    }
}
