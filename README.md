# Sorting-CUTOFF-Experiment

Benchmark MergeSort and QuickSort variants that use InsertionSort below a configurable cutoff. The experiment sweeps cutoffs 1 through 32, including the course reference values 7 and 10.

## Project layout

- `src/sorting/`: sorting implementations provided for the assignment.
- `src/experiment/`: data generation, benchmark harness, experiment runner, and CSV/report writers.
- `data/test_data/`: small deterministic sample datasets created by the runner.
- `results/results.csv`: median timing for every algorithm, distribution, input size, and cutoff.
- `reports/report.md`: experiment method, aggregate and per-workload winners, and charts.
- `reports/charts/`: SVG comparison charts generated from the measured results.

## Compile and run

From the project root, using a JDK:

```powershell
javac -encoding UTF-8 -d out src/sorting/InsertionSort.java src/sorting/MergeSort.java src/sorting/QuickSort.java src/experiment/DataGenerator.java src/experiment/Benchmark.java src/experiment/ExperimentRunner.java src/experiment/ResultWriter.java src/experiment/Main.java
java -cp out experiment.Main .
```

The runner uses 1,000, 10,000, and 50,000 element arrays across random, sorted, reverse, nearly sorted, and few-unique distributions. It performs two warmups and seven timed runs per configuration, reports the median, checks sorted output, and rewrites the data, results, report, and charts. The QuickSort implementation shuffles each array internally, so that shuffle is included in its measured time.

The fastest cutoff can vary with machine, JVM, and input distribution. Use the generated report and CSV as measurements for the machine where the experiment ran.