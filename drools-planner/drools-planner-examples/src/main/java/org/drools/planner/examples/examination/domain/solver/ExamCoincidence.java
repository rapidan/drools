package org.drools.planner.examples.examination.domain.solver;

import java.io.Serializable;
import java.util.Set;

import org.drools.planner.examples.examination.domain.Exam;

/**
 * Calculated during initialization, not used for score calculation, used for move creation.
 * @author Geoffrey De Smet
 */
public class ExamCoincidence implements Serializable {

    private Set<Exam> coincidenceExamSet;
    private Exam firstExam;

    public ExamCoincidence(Set<Exam> coincidenceExamSet) {
        this.coincidenceExamSet = coincidenceExamSet;
        for (Exam exam : coincidenceExamSet) {
            if (firstExam == null || firstExam.getId() > exam.getId()) {
                firstExam = exam;
            }
        }
    }

    public Set<Exam> getCoincidenceExamSet() {
        return coincidenceExamSet;
    }

    public void setCoincidenceExamSet(Set<Exam> coincidenceExamSet) {
        this.coincidenceExamSet = coincidenceExamSet;
    }

    public Exam getFirstExam() {
        return firstExam;
    }

    public void setFirstExam(Exam firstExam) {
        this.firstExam = firstExam;
    }
    
}