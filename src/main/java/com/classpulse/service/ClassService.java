package com.classpulse.service;

import com.classpulse.model.AcademicYear;
import com.classpulse.model.SchoolClass;
import com.classpulse.model.Teacher;
import com.classpulse.repository.SchoolClassRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ClassService {

    private static final int MAX_CLASSES = 3;

    private static final Map<AcademicYear, List<String>> SUBJECTS = Map.of(
            AcademicYear.FIRST_YEAR, List.of("Mathematics", "Physics", "Chemistry", "English"),
            AcademicYear.SECOND_YEAR, List.of("Mathematics", "Computer Science", "Biology", "English")
    );

    private final SchoolClassRepository classRepository;

    public ClassService(SchoolClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public List<String> getSubjectsForYear(AcademicYear year) {
        return SUBJECTS.getOrDefault(year, List.of());
    }

    public List<SchoolClass> getClassesByTeacher(Long teacherId) {
        return classRepository.findByTeacherId(teacherId);
    }

    public SchoolClass createClass(SchoolClass schoolClass, Teacher teacher) {
        long count = classRepository.countByTeacherId(teacher.getId());
        if (count >= MAX_CLASSES) {
            throw new IllegalStateException("You can create a maximum of " + MAX_CLASSES + " classes.");
        }
        List<String> validSubjects = SUBJECTS.get(schoolClass.getAcademicYear());
        if (!validSubjects.contains(schoolClass.getSubject())) {
            throw new IllegalArgumentException("Invalid subject for selected academic year.");
        }
        schoolClass.setTeacher(teacher);
        return classRepository.save(schoolClass);
    }

    public SchoolClass getClassByIdAndTeacher(Long classId, Long teacherId) {
        return classRepository.findByIdAndTeacherId(classId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found or access denied."));
    }

    public void deleteClass(Long classId, Long teacherId) {
        SchoolClass schoolClass = getClassByIdAndTeacher(classId, teacherId);
        classRepository.delete(schoolClass);
    }

    public long getClassCountByTeacher(Long teacherId) {
        return classRepository.countByTeacherId(teacherId);
    }
}
