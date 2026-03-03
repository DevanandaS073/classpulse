package com.classpulse.controller;

import com.classpulse.model.*;
import com.classpulse.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/classes/{classId}/tests/{testId}/marks")
public class MarkController {

    private final MarkService markService;
    private final TestService testService;
    private final StudentService studentService;
    private final ClassService classService;
    private final TeacherService teacherService;

    public MarkController(MarkService markService, TestService testService,
                          StudentService studentService, ClassService classService,
                          TeacherService teacherService) {
        this.markService = markService;
        this.testService = testService;
        this.studentService = studentService;
        this.classService = classService;
        this.teacherService = teacherService;
    }

    @GetMapping
    public String enterMarksForm(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long classId,
                                 @PathVariable Long testId,
                                 Model model) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        classService.getClassByIdAndTeacher(classId, teacher.getId());

        Test test = testService.findById(testId);
        List<Student> students = studentService.getStudentsByClass(classId);
        Map<Long, Double> existingMarks = markService.getMarksMapForTest(testId);

        model.addAttribute("test", test);
        model.addAttribute("students", students);
        model.addAttribute("existingMarks", existingMarks);
        model.addAttribute("classId", classId);
        return "enter-marks";
    }

    @GetMapping("/view")
    public String viewMarks(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long classId,
                            @PathVariable Long testId,
                            Model model) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        classService.getClassByIdAndTeacher(classId, teacher.getId());

        Test test = testService.findById(testId);
        List<Student> students = studentService.getStudentsByClass(classId);
        Map<Long, Double> existingMarks = markService.getMarksMapForTest(testId);

        model.addAttribute("test", test);
        model.addAttribute("students", students);
        model.addAttribute("existingMarks", existingMarks);
        model.addAttribute("classId", classId);
        return "view-marks";
    }

    @PostMapping("/save")
    public String saveMarks(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long classId,
                            @PathVariable Long testId,
                            @RequestParam Map<String, String> params,
                            RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        classService.getClassByIdAndTeacher(classId, teacher.getId());

        Test test = testService.findById(testId);
        List<Student> students = studentService.getStudentsByClass(classId);

        Map<Long, Double> marksMap = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("marks_")) {
                try {
                    Long studentId = Long.parseLong(entry.getKey().replace("marks_", ""));
                    Double marks = Double.parseDouble(entry.getValue());
                    marksMap.put(studentId, marks);
                } catch (NumberFormatException ignored) {}
            }
        }

        try {
            markService.saveMarks(test, students, marksMap);
            redirectAttributes.addFlashAttribute("success", "Marks saved successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/classes/" + classId;
    }
}
