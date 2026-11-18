package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.learning.AssessmentQuestion;
import com.aischolartwin.entity.learning.LearningAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    List<AssessmentQuestion> findByAssessmentOrderByQuestionOrderAsc(LearningAssessment assessment);
}
