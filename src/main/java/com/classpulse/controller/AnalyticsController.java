package com.classpulse.controller;

import com.classpulse.model.*;
import com.classpulse.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ClassService classService;
    private final TeacherService teacherService;
    private final TestService testService;

    public AnalyticsController(AnalyticsService analyticsService, ClassService classService,
                               TeacherService teacherService, TestService testService) {
        this.analyticsService = analyticsService;
        this.classService = classService;
        this.teacherService = teacherService;
        this.testService = testService;
    }

    /** Full class analytics – based on ALL tests ever taken */
    @GetMapping("/classes/{classId}/analytics")
    public String analytics(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long classId, Model model) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        SchoolClass schoolClass = classService.getClassByIdAndTeacher(classId, teacher.getId());
        AnalyticsService.ClassAnalytics analytics = analyticsService.computeClassAnalytics(classId);

        model.addAttribute("schoolClass", schoolClass);
        model.addAttribute("analytics", analytics);
        model.addAttribute("classId", classId);
        return "analytics";
    }

    /** Per-test analytics – stats for one individual test */
    @GetMapping("/classes/{classId}/tests/{testId}/analytics")
    public String testAnalytics(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long classId,
                                @PathVariable Long testId,
                                Model model) {
        Teacher teacher = teacherService.findByEmail(userDetails.getUsername());
        SchoolClass schoolClass = classService.getClassByIdAndTeacher(classId, teacher.getId());
        Test test = testService.findById(testId);
        AnalyticsService.TestAnalytics testAnalytics = analyticsService.computeTestAnalytics(testId, classId);

        model.addAttribute("schoolClass", schoolClass);
        model.addAttribute("test", test);
        model.addAttribute("testAnalytics", testAnalytics);
        model.addAttribute("classId", classId);
        return "test-analytics";
    }
}
