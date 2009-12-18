package org.drools.planner.examples.itc2007.examination.persistence;

import java.io.IOException;
import java.util.Collections;

import org.drools.planner.examples.common.domain.PersistableIdComparator;
import org.drools.planner.examples.common.persistence.AbstractOutputConvertor;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.drools.planner.examples.itc2007.examination.domain.Exam;
import org.drools.planner.examples.itc2007.examination.domain.Examination;
import org.drools.planner.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationOutputConvertor extends AbstractOutputConvertor {

    private static final String OUTPUT_FILE_SUFFIX = ".sln";

    public static void main(String[] args) {
        new ExaminationOutputConvertor().convertAll();
    }

    public ExaminationOutputConvertor() {
        super(new ExaminationDaoImpl());
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public OutputBuilder createOutputBuilder() {
        return new ExaminationOutputBuilder();
    }

    public class ExaminationOutputBuilder extends OutputBuilder {

        private Examination examination;

        public void setSolution(Solution solution) {
            examination = (Examination) solution;
        }

        public void writeSolution() throws IOException {
            Collections.sort(examination.getExamList(), new PersistableIdComparator()); // TODO remove me when obsolete
            for (Exam exam : examination.getExamList()) {
                bufferedWriter.write(exam.getPeriod().getId() + ", " + exam.getRoom().getId() + "\r\n");
            }
        }
        
    }

}