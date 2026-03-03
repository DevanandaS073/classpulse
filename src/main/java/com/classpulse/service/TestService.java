package com.classpulse.service;

import com.classpulse.model.SchoolClass;
import com.classpulse.model.Test;
import com.classpulse.repository.TestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public List<Test> getTestsByClass(Long classId) {
        return testRepository.findBySchoolClassIdOrderByTestDateAsc(classId);
    }

    public Test createTest(Test test, SchoolClass schoolClass) {
        test.setSchoolClass(schoolClass);
        return testRepository.save(test);
    }

    public Test findById(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));
    }

    public void deleteTest(Long id) {
        testRepository.deleteById(id);
    }
}
