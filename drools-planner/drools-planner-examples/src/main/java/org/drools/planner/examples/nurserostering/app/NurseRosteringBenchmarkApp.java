package org.drools.planner.examples.nurserostering.app;

import java.io.File;

import org.drools.planner.examples.common.app.CommonBenchmarkApp;
import org.drools.planner.examples.nurserostering.domain.NurseRoster;

/**
 * @author Geoffrey De Smet
 */
public class NurseRosteringBenchmarkApp extends CommonBenchmarkApp {

    public static final String SPRINT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/nurserostering/benchmark/nurseRosteringSprintSolverBenchmarkConfig.xml";
    public static final String MEDIUM_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/nurserostering/benchmark/nurseRosteringMediumSolverBenchmarkConfig.xml";
    public static final String LONG_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/nurserostering/benchmark/nurseRosteringLongSolverBenchmarkConfig.xml";
    public static final String STEP_LIMIT_SOLVER_BENCHMARK_CONFIG
            = "/org/drools/planner/examples/nurserostering/benchmark/nurseRosteringStepLimitSolverBenchmarkConfig.xml";

    public static void main(String[] args) {
        String solverConfig;
        if (args.length > 0) {
            if (args[0].equals("sprint")) {
                solverConfig = SPRINT_SOLVER_BENCHMARK_CONFIG;
            } else if (args[0].equals("medium")) {
                solverConfig = MEDIUM_SOLVER_BENCHMARK_CONFIG;
            } else if (args[0].equals("long")) {
                solverConfig = LONG_SOLVER_BENCHMARK_CONFIG;
            } else if (args[0].equals("stepLimit")) {
                solverConfig = STEP_LIMIT_SOLVER_BENCHMARK_CONFIG;
            } else {
                throw new IllegalArgumentException("The program argument (" + args[0] + ") is not supported.");
            }
        } else {
            solverConfig = MEDIUM_SOLVER_BENCHMARK_CONFIG;
        }
        new NurseRosteringBenchmarkApp(solverConfig).process();
    }

    public NurseRosteringBenchmarkApp(String solverBenchmarkConfig) {
        super(solverBenchmarkConfig, NurseRoster.class);
    }

}
