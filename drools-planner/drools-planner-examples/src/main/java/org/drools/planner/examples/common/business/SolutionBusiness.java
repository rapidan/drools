package org.drools.planner.examples.common.business;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.localsearch.DefaultLocalSearchSolver;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.constraint.ConstraintOccurrence;
import org.drools.planner.core.score.constraint.DoubleConstraintOccurrence;
import org.drools.planner.core.score.constraint.IntConstraintOccurrence;
import org.drools.planner.core.score.constraint.UnweightedConstraintOccurrence;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.AbstractSolutionExporter;
import org.drools.planner.examples.common.persistence.AbstractSolutionImporter;
import org.drools.planner.examples.common.persistence.SolutionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class SolutionBusiness {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private SolutionDao solutionDao;

    private AbstractSolutionImporter importer;
    private AbstractSolutionExporter exporter;

    private File importDataDir;
    private File unsolvedDataDir;
    private File solvedDataDir;
    private File exportDataDir;

    // volatile because the solve method doesn't come from the event thread (like every other method call)
    private volatile Solver solver;
    private LocalSearchSolverScope localSearchSolverScope;

    public void setSolutionDao(SolutionDao solutionDao) {
        this.solutionDao = solutionDao;
    }

    public void setImporter(AbstractSolutionImporter importer) {
        this.importer = importer;
    }

    public void setExporter(AbstractSolutionExporter exporter) {
        this.exporter = exporter;
    }

    public File getDataDir() {
        return solutionDao.getDataDir();
    }

    public String getDirName() {
        return solutionDao.getDirName();
    }

    public boolean hasImporter() {
        return importer != null;
    }

    public boolean hasExporter() {
        return exporter != null;
    }

    public void updateDataDirs() {
        if (hasImporter()) {
            importDataDir = new File(getDataDir(), "input");
            if (!importDataDir.exists()) {
                throw new IllegalStateException("The directory importDataDir (" + importDataDir.getAbsolutePath()
                        + ") does not exist. The working directory should be set to drools-planner-examples.");
            }
        }
        unsolvedDataDir = new File(getDataDir(), "unsolved");
        if (!unsolvedDataDir.exists()) {
            throw new IllegalStateException("The directory unsolvedDataDir (" + unsolvedDataDir.getAbsolutePath()
                    + ") does not exist. The working directory should be set to drools-planner-examples.");
        }
        solvedDataDir = new File(getDataDir(), "solved");
        if (!solvedDataDir.exists()) {
            throw new IllegalStateException("The directory solvedDataDir (" + solvedDataDir.getAbsolutePath()
                    + ") does not exist. The working directory should be set to drools-planner-examples.");
        }
        if (hasExporter()) {
            exportDataDir = new File(getDataDir(), "output");
            if (!exportDataDir.exists()) {
                throw new IllegalStateException("The directory exportDataDir (" + exportDataDir.getAbsolutePath()
                        + ") does not exist. The working directory should be set to drools-planner-examples.");
            }
        }
    }

    public File getImportDataDir() {
        return importDataDir;
    }

    public File getUnsolvedDataDir() {
        return unsolvedDataDir;
    }

    public File getSolvedDataDir() {
        return solvedDataDir;
    }

    public File getExportDataDir() {
        return exportDataDir;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
        this.localSearchSolverScope = ((DefaultLocalSearchSolver) solver).getLocalSearchSolverScope();
    }


    public List<File> getUnsolvedFileList() {
        List<File> unsolvedFileList = Arrays.asList(unsolvedDataDir.listFiles(new SolverExampleFileFilter()));
        Collections.sort(unsolvedFileList);
        return unsolvedFileList;
    }

    public List<File> getSolvedFileList() {
        List<File> solvedFileList = Arrays.asList(solvedDataDir.listFiles(new SolverExampleFileFilter()));
        Collections.sort(solvedFileList);
        return solvedFileList;
    }

    public Solution getSolution() {
        return localSearchSolverScope.getWorkingSolution();
    }

    public Score getScore() {
        return localSearchSolverScope.calculateScoreFromWorkingMemory();
    }

    public void addSolverEventLister(SolverEventListener eventListener) {
        solver.addEventListener(eventListener);
    }

    public List<ScoreDetail> getScoreDetailList() {
        Map<String, ScoreDetail> scoreDetailMap = new HashMap<String, ScoreDetail>();
        WorkingMemory workingMemory = localSearchSolverScope.getWorkingMemory();
        if (workingMemory == null) {
            return Collections.emptyList();
        }
        Iterator<ConstraintOccurrence> it = (Iterator<ConstraintOccurrence>) workingMemory.iterateObjects(
                new ClassObjectFilter(ConstraintOccurrence.class));
        while (it.hasNext()) {
            ConstraintOccurrence occurrence = it.next();
            ScoreDetail scoreDetail = scoreDetailMap.get(occurrence.getRuleId());
            if (scoreDetail == null) {
                scoreDetail = new ScoreDetail(occurrence.getRuleId(), occurrence.getConstraintType());
                scoreDetailMap.put(occurrence.getRuleId(), scoreDetail);
            }
            double occurrenceScore;
            if (occurrence instanceof IntConstraintOccurrence) {
                occurrenceScore = ((IntConstraintOccurrence) occurrence).getWeight();
            } else if (occurrence instanceof DoubleConstraintOccurrence) {
                occurrenceScore = ((DoubleConstraintOccurrence) occurrence).getWeight();
            } else if (occurrence instanceof UnweightedConstraintOccurrence) {
                occurrenceScore = 1.0;
            } else {
                throw new IllegalStateException("Cannot determine occurrenceScore of ConstraintOccurrence class: "
                        + occurrence.getClass());
            }
            scoreDetail.addOccurrenceScore(occurrenceScore);
        }
        List<ScoreDetail> scoreDetailList = new ArrayList<ScoreDetail>(scoreDetailMap.values());
        Collections.sort(scoreDetailList);
        return scoreDetailList;
    }

    public void importSolution(File file) {
        Solution solution = importer.readSolution(file);
        solver.setStartingSolution(solution);
    }

    public void loadSolution(File file) {
        Solution solution = solutionDao.readSolution(file);
        solver.setStartingSolution(solution);
    }

    public void saveSolution(File file) {
        Solution solution = localSearchSolverScope.getWorkingSolution();
        solutionDao.writeSolution(solution, file);
    }

    public void exportSolution(File file) {
        Solution solution = localSearchSolverScope.getWorkingSolution();
        exporter.writeSolution(solution, file);
    }

    public void doMove(Move move) {
        if (!move.isMoveDoable(localSearchSolverScope.getWorkingMemory())) {
            logger.info("Not doing user move ({}) because it is not doable.", move);
            return;
        }
        logger.info("Doing user move ({}).", move);
        move.doMove(localSearchSolverScope.getWorkingMemory());
    }

    public void solve() {
        solver.solve();
        Solution solution = solver.getBestSolution();
        solver.setStartingSolution(solution);
    }

    public void terminateSolvingEarly() {
        solver.terminateEarly();
    }

    public class SolverExampleFileFilter implements FileFilter {

        public boolean accept(File file) {
            if (file.isDirectory() || file.isHidden()) {
                return false;
            }
            return file.getName().endsWith(".xml");
        }

    }
    
}
