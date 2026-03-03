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
@RequestMapping("/classes/{classId}/tests")
public class TestController {

    private final TestService testService;
    private final ClassService classService;
    private final TeacherService teacherService;

    public TestController(TestService testService, ClassService classService, TeacherService teacherService) {
        this.testService = testService;
        this.classService = classService;
        this.teacherService = teacherService;
    }

    @PostMapping("/add")
    public String addTest(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable Long classId,
                          @Valid @ModelAttribute("newTest") Test test,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        SchoolClass schoolClass = classService.getClassByIdAndTeacher(classId, teacher.getId());

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("testError", "Please fill all test fields correctly.");
            return "redirect:/classes/" + classId;
        }

        testService.createTest(test, schoolClass);
        redirectAttributes.addFlashAttribute("success", "Test added successfully.");
        return "redirect:/classes/" + classId;
    }

    @PostMapping("/{testId}/delete")
    public String deleteTest(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long classId,
                             @PathVariable Long testId,
                             RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        classService.getClassByIdAndTeacher(classId, teacher.getId()); // ownership check
        testService.deleteTest(testId);
        redirectAttributes.addFlashAttribute("success", "Test deleted.");
        return "redirect:/classes/" + classId;
    }
}
