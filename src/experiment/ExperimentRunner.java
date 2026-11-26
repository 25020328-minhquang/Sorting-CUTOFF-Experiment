package experiment;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ExperimentRunner {
    private static final int[] SIZES = {1000, 10000, 50000};
    private static final int MIN_CUTOFF = 1;
    private static final int MAX_CUTOFF = 32;
    private static final int WARMUPS = 2;
    private static final int REPETITIONS = 7;
    private static final long SEED = 20261126L;

    private ExperimentRunner() { }

    public static void run(Path projectRoot) throws IOException {
        DataGenerator.writeSampleData(projectRoot.resolve("data/test_data"));
        List<Benchmark.Result> results = new ArrayList<Benchmark.Result>();
        String[] algorithms = {"MergeSort", "QuickSort"};
        for (String distribution : DataGenerator.DISTRIBUTIONS) {
            for (int size : SIZES) {
                int[] input = DataGenerator.generate(size, distribution, SEED + size);
                for (String algorithm : algorithms) {
                    for (int cutoff = MIN_CUTOFF; cutoff <= MAX_CUTOFF; cutoff++) {
                        results.add(Benchmark.measure(algorithm, distribution, input, cutoff, WARMUPS, REPETITIONS));
                    }
                }
            }
        }
        ResultWriter.write(projectRoot, results, MIN_CUTOFF, MAX_CUTOFF, SIZES, DataGenerator.DISTRIBUTIONS, WARMUPS, REPETITIONS, SEED);
        System.out.println("Experiment complete: " + results.size() + " benchmark configurations.");
        System.out.println("Results: " + projectRoot.resolve("results/results.csv").toAbsolutePath());
        System.out.println("Report:  " + projectRoot.resolve("reports/report.md").toAbsolutePath());
    }

    public static void main(String[] args) throws IOException {
        Path root = args.length == 0 ? Paths.get(".") : Paths.get(args[0]);
        run(root.toAbsolutePath().normalize());
    }
}