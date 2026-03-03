package com.classpulse.service;

import com.classpulse.model.Mark;
import com.classpulse.model.Student;
import com.classpulse.model.Test;
import com.classpulse.repository.MarkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarkService {

    private final MarkRepository markRepository;

    public MarkService(MarkRepository markRepository) {
        this.markRepository = markRepository;
    }

    public List<Mark> getMarksByTest(Long testId) {
        return markRepository.findByTestId(testId);
    }

    public List<Mark> getMarksByStudent(Long studentId) {
        return markRepository.findByStudentId(studentId);
    }

    public void saveMarks(Test test, List<Student> students, Map<Long, Double> marksMap) {
        for (Student student : students) {
            Double marksObtained = marksMap.get(student.getId());
            if (marksObtained == null) continue;

            if (marksObtained < 0 || marksObtained > test.getMaxMarks()) {
                throw new IllegalArgumentException(
                    "Marks for " + student.getName() + " must be between 0 and " + test.getMaxMarks()
                );
            }

            Optional<Mark> existing = markRepository.findByStudentIdAndTestId(student.getId(), test.getId());
            if (existing.isPresent()) {
                existing.get().setMarksObtained(marksObtained);
                markRepository.save(existing.get());
            } else {
                Mark mark = new Mark();
                mark.setStudent(student);
                mark.setTest(test);
                mark.setMarksObtained(marksObtained);
                markRepository.save(mark);
            }
        }
    }

    public Map<Long, Double> getMarksMapForTest(Long testId) {
        List<Mark> marks = markRepository.findByTestId(testId);
        Map<Long, Double> map = new java.util.HashMap<>();
        for (Mark m : marks) {
            map.put(m.getStudent().getId(), m.getMarksObtained());
        }
        return map;
    }
}
