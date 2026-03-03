package com.classpulse.service;

import com.classpulse.model.*;
import com.classpulse.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final TestRepository testRepository;
    private final MarkRepository markRepository;

    public AnalyticsService(StudentRepository studentRepository,
                             TestRepository testRepository,
                             MarkRepository markRepository) {
        this.studentRepository = studentRepository;
        this.testRepository = testRepository;
        this.markRepository = markRepository;
    }

    public static class StudentStats {
        public Long studentId;
        public String studentName;
        public String rollNumber;
        public double totalMarks;
        public double totalMaxMarks;
        public double average;
        public double percentage;
        public double highestScore;
        public double lowestScore;
        public String category;
        public List<Double> scoreHistory = new ArrayList<>();
    }

    public static class ClassAnalytics {
        public List<StudentStats> studentStatsList = new ArrayList<>();
        public List<String> testNames = new ArrayList<>();
        public List<Double> classAveragePerTest = new ArrayList<>();
        public double overallClassAverage;
        public StudentStats topStudent;
        public StudentStats bottomStudent;
        public List<StudentStats> studentsBelow40 = new ArrayList<>();
        public List<StudentStats> top5Students = new ArrayList<>();
        public int totalStudents;
        public int totalTests;
    }

    public ClassAnalytics computeClassAnalytics(Long classId) {
        List<Student> students = studentRepository.findBySchoolClassIdOrderByRollNumberAsc(classId);
        List<Test> tests = testRepository.findBySchoolClassIdOrderByTestDateAsc(classId);

        ClassAnalytics analytics = new ClassAnalytics();
        analytics.totalStudents = students.size();
        analytics.totalTests = tests.size();

        if (students.isEmpty() || tests.isEmpty()) {
            return analytics;
        }

        // test names for charts
        for (Test t : tests) {
            analytics.testNames.add(t.getName() + " (" + t.getMaxMarks() + ")");
        }

        // build a map: studentId -> list of marks per test (in order)
        Map<Long, Map<Long, Double>> marksMatrix = new HashMap<>();
        for (Student s : students) {
            marksMatrix.put(s.getId(), new HashMap<>());
        }

        List<Long> testIds = tests.stream().map(Test::getId).collect(Collectors.toList());
        List<Mark> allMarks = markRepository.findByTestIdIn(testIds);
        for (Mark m : allMarks) {
            Long sid = m.getStudent().getId();
            if (marksMatrix.containsKey(sid)) {
                marksMatrix.get(sid).put(m.getTest().getId(), m.getMarksObtained());
            }
        }

        // per-test class averages
        for (Test test : tests) {
            double sum = 0;
            int count = 0;
            for (Student s : students) {
                Double mark = marksMatrix.get(s.getId()).get(test.getId());
                if (mark != null) {
                    sum += mark;
                    count++;
                }
            }
            analytics.classAveragePerTest.add(count > 0 ? Math.round((sum / count) * 100.0) / 100.0 : 0.0);
        }

        // per-student stats
        double totalMarksAllStudents = 0;
        int countStudentsWithMarks = 0;

        for (Student s : students) {
            StudentStats stats = new StudentStats();
            stats.studentId = s.getId();
            stats.studentName = s.getName();
            stats.rollNumber = s.getRollNumber();

            Map<Long, Double> studentMarks = marksMatrix.get(s.getId());
            double total = 0, totalMax = 0, highest = 0, lowest = Double.MAX_VALUE;
            boolean hasMarks = false;

            for (Test test : tests) {
                Double mark = studentMarks.get(test.getId());
                stats.scoreHistory.add(mark != null ? mark : 0.0);
                if (mark != null) {
                    total += mark;
                    totalMax += test.getMaxMarks();
                    highest = Math.max(highest, mark);
                    lowest = Math.min(lowest, mark);
                    hasMarks = true;
                }
            }

            stats.totalMarks = Math.round(total * 100.0) / 100.0;
            stats.totalMaxMarks = totalMax;
            stats.average = hasMarks ? Math.round((total / tests.size()) * 100.0) / 100.0 : 0;
            stats.percentage = totalMax > 0 ? Math.round((total / totalMax) * 10000.0) / 100.0 : 0;
            stats.highestScore = hasMarks ? highest : 0;
            stats.lowestScore = hasMarks && lowest != Double.MAX_VALUE ? lowest : 0;
            stats.category = getCategory(stats.percentage);

            analytics.studentStatsList.add(stats);

            if (hasMarks) {
                totalMarksAllStudents += stats.percentage;
                countStudentsWithMarks++;
            }
        }

        // overall class average
        analytics.overallClassAverage = countStudentsWithMarks > 0
                ? Math.round((totalMarksAllStudents / countStudentsWithMarks) * 100.0) / 100.0
                : 0;

        // sort by percentage desc
        List<StudentStats> sorted = analytics.studentStatsList.stream()
                .sorted(Comparator.comparingDouble((StudentStats st) -> st.percentage).reversed())
                .collect(Collectors.toList());

        if (!sorted.isEmpty()) {
            analytics.topStudent = sorted.get(0);
            analytics.bottomStudent = sorted.get(sorted.size() - 1);
            analytics.top5Students = sorted.stream().limit(5).collect(Collectors.toList());
        }

        analytics.studentsBelow40 = analytics.studentStatsList.stream()
                .filter(st -> st.percentage < 40 && st.totalMaxMarks > 0)
                .collect(Collectors.toList());

        return analytics;
    }

    public StudentStats computeStudentStats(Long studentId, Long classId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        List<Test> tests = testRepository.findBySchoolClassIdOrderByTestDateAsc(classId);

        StudentStats stats = new StudentStats();
        stats.studentId = student.getId();
        stats.studentName = student.getName();
        stats.rollNumber = student.getRollNumber();

        List<Mark> marks = markRepository.findByStudentId(studentId);
        Map<Long, Double> marksMap = new HashMap<>();
        for (Mark m : marks) {
            marksMap.put(m.getTest().getId(), m.getMarksObtained());
        }

        double total = 0, totalMax = 0, highest = 0, lowest = Double.MAX_VALUE;
        boolean hasMarks = false;

        for (Test test : tests) {
            Double mark = marksMap.get(test.getId());
            stats.scoreHistory.add(mark != null ? mark : 0.0);
            if (mark != null) {
                total += mark;
                totalMax += test.getMaxMarks();
                highest = Math.max(highest, mark);
                lowest = Math.min(lowest, mark);
                hasMarks = true;
            }
        }

        stats.totalMarks = Math.round(total * 100.0) / 100.0;
        stats.totalMaxMarks = totalMax;
        stats.average = hasMarks ? Math.round((total / tests.size()) * 100.0) / 100.0 : 0;
        stats.percentage = totalMax > 0 ? Math.round((total / totalMax) * 10000.0) / 100.0 : 0;
        stats.highestScore = hasMarks ? highest : 0;
        stats.lowestScore = hasMarks && lowest != Double.MAX_VALUE ? lowest : 0;
        stats.category = getCategory(stats.percentage);
        return stats;
    }

    private String getCategory(double percentage) {
        if (percentage >= 90) return "Excellent";
        if (percentage >= 75) return "Very Good";
        if (percentage >= 50) return "Average";
        return "Needs Improvement";
    }

    // ─── Per-Test Analytics ────────────────────────────────────────────────

    public static class TestStudentRow {
        public Long studentId;
        public String studentName;
        public String rollNumber;
        public Double marksObtained;   // null = not entered
        public double percentage;
        public String category;
    }

    public static class MarkRange {
        public String label;          // e.g. "1 – 10"
        public double from;
        public double to;
        public List<TestStudentRow> students = new ArrayList<>();
        public String rangeCategory;  // Excellent / Very Good / Average / Needs Improvement
    }

    public static class TestAnalytics {
        public List<TestStudentRow> rows = new ArrayList<>();
        public double classAverage;
        public double highestMark;
        public double lowestMark;
        public String topStudentName;
        public String bottomStudentName;
        public int totalStudents;
        public int marksEntered;
        public List<String> studentNames = new ArrayList<>();
        public List<Double> studentMarks = new ArrayList<>();
        public List<TestStudentRow> studentsBelow40 = new ArrayList<>();
        public List<MarkRange> markRanges = new ArrayList<>();
    }

    public TestAnalytics computeTestAnalytics(Long testId, Long classId) {
        List<Student> students = studentRepository.findBySchoolClassIdOrderByRollNumberAsc(classId);
        List<Mark> marks = markRepository.findByTestId(testId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));

        Map<Long, Double> marksMap = new HashMap<>();
        for (Mark m : marks) {
            marksMap.put(m.getStudent().getId(), m.getMarksObtained());
        }

        TestAnalytics analytics = new TestAnalytics();
        analytics.totalStudents = students.size();

        double sum = 0;
        int count = 0;
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;
        String topName = "-", bottomName = "-";

        for (Student s : students) {
            TestStudentRow row = new TestStudentRow();
            row.studentId = s.getId();
            row.studentName = s.getName();
            row.rollNumber = s.getRollNumber();
            row.marksObtained = marksMap.get(s.getId());

            if (row.marksObtained != null) {
                double pct = (row.marksObtained / test.getMaxMarks()) * 100.0;
                row.percentage = Math.round(pct * 100.0) / 100.0;
                row.category = getCategory(row.percentage);
                sum += row.marksObtained;
                count++;
                if (row.marksObtained > highest) { highest = row.marksObtained; topName = s.getName(); }
                if (row.marksObtained < lowest)  { lowest  = row.marksObtained; bottomName = s.getName(); }
                analytics.studentNames.add(s.getName());
                analytics.studentMarks.add(row.marksObtained);
                if (row.percentage < 40) analytics.studentsBelow40.add(row);
            } else {
                row.percentage = 0;
                row.category = "-";
            }

            analytics.rows.add(row);
        }

        analytics.marksEntered = count;
        analytics.classAverage = count > 0 ? Math.round((sum / count) * 100.0) / 100.0 : 0;
        analytics.highestMark = highest == Double.MIN_VALUE ? 0 : highest;
        analytics.lowestMark  = lowest  == Double.MAX_VALUE ? 0 : lowest;
        analytics.topStudentName    = topName;
        analytics.bottomStudentName = bottomName;

        // ── Mark Range Classification (3 equal bands) ──────────────────────
        double max = test.getMaxMarks();
        double band = max / 3.0;
        double[] fromArr = { 1,              Math.floor(band) + 1,       Math.floor(band * 2) + 1 };
        double[] toArr   = { Math.floor(band), Math.floor(band * 2),      max                      };
        String[] cats    = { "Needs Improvement", "Average", "Excellent" };
        // If mid-range maps to >=75% treat it Very Good
        if ((toArr[1] / max) * 100 >= 75) cats[1] = "Very Good";

        for (int i = 0; i < 3; i++) {
            MarkRange range = new MarkRange();
            range.from = fromArr[i];
            range.to   = toArr[i];
            range.label = (int) fromArr[i] + " – " + (int) toArr[i];
            range.rangeCategory = cats[i];
            final double lo = fromArr[i], hi = toArr[i];
            for (TestStudentRow r : analytics.rows) {
                if (r.marksObtained != null && r.marksObtained >= lo && r.marksObtained <= hi) {
                    range.students.add(r);
                }
            }
            analytics.markRanges.add(range);
        }
        // Reverse so highest range appears first
        java.util.Collections.reverse(analytics.markRanges);

        return analytics;
    }
}
