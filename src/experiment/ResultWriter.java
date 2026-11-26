package experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ResultWriter {
    private ResultWriter() { }

    public static void write(Path root, List<Benchmark.Result> rows, int minCutoff, int maxCutoff,
            int[] sizes, String[] distributions, int warmups, int repetitions, long seed) throws IOException {
        Path resultsDir = root.resolve("results");
        Path reportDir = root.resolve("reports");
        Path chartsDir = reportDir.resolve("charts");
        Files.createDirectories(resultsDir); Files.createDirectories(chartsDir);
        StringBuilder csv = new StringBuilder("algorithm,distribution,size,cutoff,median_ns,repetitions\n");
        for (Benchmark.Result r : rows) {
            csv.append(r.algorithm).append(',').append(r.distribution).append(',').append(r.size).append(',')
                    .append(r.cutoff).append(',').append(r.medianNanos).append(',').append(r.repetitions).append('\n');
        }
        Files.write(resultsDir.resolve("results.csv"), csv.toString().getBytes("UTF-8"));
        Map<String, Double> scores = normalizedScores(rows, minCutoff, maxCutoff, distributions, sizes);
        StringBuilder report = new StringBuilder();
        report.append("# MergeSort and QuickSort cutoff experiment\n\n")
                .append("## Goal\n\nCompare the lecture-style MergeSort and QuickSort implementations while they switch to InsertionSort for small partitions. The sweep includes the course reference cutoffs 7 and 10 and searches every integer cutoff from ")
                .append(minCutoff).append(" through ").append(maxCutoff).append(".\n\n")
                .append("## Method\n\n")
                .append("- Algorithms: MergeSort and QuickSort, using the implementations in `src/sorting`.\n")
                .append("- Input sizes: ").append(join(sizes)).append(" integers.\n")
                .append("- Distributions: ").append(join(distributions)).append(". The nearly sorted case starts sorted and applies about 1% random swaps; few_unique uses 16 possible values.\n")
                .append("- Each configuration uses ").append(warmups).append(" warmup runs and ").append(repetitions).append(" measured runs. The reported value is the median elapsed time in nanoseconds. Input copying and correctness checks are outside the timed section.\n")
                .append("- Data seed: base seed ").append(seed).append(" plus input size for benchmark arrays; sample CSV files use the base seed. QuickSort also shuffles internally using its current implementation.\n\n")
                .append("## Summary\n\n")
                .append("The overall score is the mean of each configuration runtime divided by the fastest runtime for that same algorithm, input size, and distribution. Lower is better; this gives each workload equal weight. A best cutoff at either sweep endpoint (1 or 32) means the search range should be expanded before treating it as optimal.\n\n");
                report.append("| Algorithm | Best aggregate cutoff | Mean normalized runtime |\n|---|---:|---:|\n");
        for (String algorithm : new String[]{"MergeSort", "QuickSort"}) {
            int best = minCutoff; double bestScore = Double.MAX_VALUE;
            for (int c = minCutoff; c <= maxCutoff; c++) {
                double s = scores.get(algorithm + ":" + c);
                if (s < bestScore) { bestScore = s; best = c; }
            }
            report.append("| ").append(algorithm).append(" | ").append(best).append(" | ").append(String.format(java.util.Locale.US, "%.4f", bestScore)).append(" |\n");
        }
        report.append("\nThe best cutoff can vary with CPU, JVM, and workload. Treat these values as results from the machine that ran this experiment, not as universal constants. Review `results.csv` before drawing conclusions.\n\n");
        report.append("## Course cutoff comparison\n\n| Algorithm | Cutoff | Mean normalized runtime |\n|---|---:|---:|\n");
        for (String algorithm : new String[]{"MergeSort", "QuickSort"}) for (int cutoff : new int[]{7, 10}) {
            report.append("| ").append(algorithm).append(" | ").append(cutoff).append(" | ").append(String.format(java.util.Locale.US, "%.4f", scores.get(algorithm + ":" + cutoff))).append(" |\n");
        }
        report.append("\nLower normalized runtime is better. Cutoffs 7 and 10 are also highlighted in orange in the charts.\n\n")
                .append("## Charts\n\n")
                .append("![MergeSort cutoff comparison](charts/merge_cutoff.svg)\n\n")
                .append("![QuickSort cutoff comparison](charts/quick_cutoff.svg)\n\n")
                .append("## Detailed best cutoff by workload\n\n| Algorithm | Distribution | Size | Best cutoff | Median time (ns) |\n|---|---|---:|---:|---:|\n");
        appendWorkloadWinners(report, rows);
        report.append("\n## Reproducibility\n\nRun `java -cp out experiment.Main` from the project root after compiling the source files. This regenerates sample data, `results/results.csv`, this report, and both SVG charts.\n");
        Files.write(reportDir.resolve("report.md"), report.toString().getBytes("UTF-8"));
        writeChart(chartsDir.resolve("merge_cutoff.svg"), "MergeSort", scores, minCutoff, maxCutoff);
        writeChart(chartsDir.resolve("quick_cutoff.svg"), "QuickSort", scores, minCutoff, maxCutoff);
    }

    private static Map<String, Double> normalizedScores(List<Benchmark.Result> rows, int min, int max, String[] distributions, int[] sizes) {
        Map<String, Long> best = new HashMap<String, Long>();
        for (Benchmark.Result r : rows) {
            String key = workload(r.algorithm, r.distribution, r.size);
            Long previous = best.get(key);
            if (previous == null || r.medianNanos < previous) best.put(key, r.medianNanos);
        }
        Map<String, Double> totals = new HashMap<String, Double>();
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (Benchmark.Result r : rows) {
            String scoreKey = r.algorithm + ":" + r.cutoff;
            String workloadKey = workload(r.algorithm, r.distribution, r.size);
            double ratio = (double) r.medianNanos / Math.max(1L, best.get(workloadKey));
            Double old = totals.get(scoreKey); totals.put(scoreKey, (old == null ? 0.0 : old) + ratio);
            Integer n = counts.get(scoreKey); counts.put(scoreKey, (n == null ? 0 : n) + 1);
        }
        Map<String, Double> scores = new HashMap<String, Double>();
        for (String algorithm : new String[]{"MergeSort", "QuickSort"}) for (int c = min; c <= max; c++) {
            String key = algorithm + ":" + c;
            scores.put(key, totals.get(key) / counts.get(key));
        }
        return scores;
    }

    private static String workload(String algorithm, String distribution, int size) { return algorithm + ":" + distribution + ":" + size; }

    private static void appendWorkloadWinners(StringBuilder report, List<Benchmark.Result> rows) {
        Map<String, Benchmark.Result> winners = new HashMap<String, Benchmark.Result>();
        for (Benchmark.Result r : rows) {
            String key = workload(r.algorithm, r.distribution, r.size);
            Benchmark.Result old = winners.get(key);
            if (old == null || r.medianNanos < old.medianNanos) winners.put(key, r);
        }
        for (Benchmark.Result r : rows) {
            String key = workload(r.algorithm, r.distribution, r.size);
            if (winners.get(key) == r) report.append("| ").append(r.algorithm).append(" | ").append(r.distribution).append(" | ").append(r.size)
                    .append(" | ").append(r.cutoff).append(" | ").append(r.medianNanos).append(" |\n");
        }
    }

    private static void writeChart(Path path, String algorithm, Map<String, Double> scores, int min, int max) throws IOException {
        int width = 1000, height = 420, left = 65, top = 45, bottom = 65;
        double maxScore = 1.0;
        for (int c = min; c <= max; c++) maxScore = Math.max(maxScore, scores.get(algorithm + ":" + c));
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width).append("\" height=\"").append(height).append("\" viewBox=\"0 0 ").append(width).append(' ').append(height).append("\">\n")
                .append("<rect width=\"100%\" height=\"100%\" fill=\"white\"/><text x=\"500\" y=\"25\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"18\">").append(algorithm).append(" mean normalized runtime by cutoff</text>\n")
                .append("<line x1=\"").append(left).append("\" y1=\"").append(height - bottom).append("\" x2=\"970\" y2=\"").append(height - bottom).append("\" stroke=\"#333\"/><line x1=\"").append(left).append("\" y1=\"").append(top).append("\" x2=\"").append(left).append("\" y2=\"").append(height - bottom).append("\" stroke=\"#333\"/>\n");
        double plotHeight = height - top - bottom, plotWidth = 900.0;
        for (int tick = 0; tick <= 4; tick++) {
            double value = maxScore * tick / 4.0, y = height - bottom - plotHeight * tick / 4.0;
            svg.append("<line x1=\"").append(left).append("\" y1=\"").append(y).append("\" x2=\"965\" y2=\"").append(y).append("\" stroke=\"#ddd\"/><text x=\"55\" y=\"").append(y + 4).append("\" text-anchor=\"end\" font-family=\"Arial\" font-size=\"11\">").append(String.format(java.util.Locale.US, "%.2f", value)).append("</text>\n");
        }
        double slot = plotWidth / (max - min + 1), barWidth = Math.max(3, slot - 5);
        for (int c = min; c <= max; c++) {
            double value = scores.get(algorithm + ":" + c), barHeight = plotHeight * value / maxScore;
            double x = left + (c - min) * slot + (slot - barWidth) / 2, y = height - bottom - barHeight;
            String color = c == 7 || c == 10 ? "#e69500" : "#3977b8";
            svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(barWidth).append("\" height=\"").append(barHeight).append("\" fill=\"").append(color).append("\"><title>cutoff ").append(c).append(": ").append(String.format(java.util.Locale.US, "%.4f", value)).append("</title></rect>\n");
            if (c == min || c % 2 == 0) svg.append("<text x=\"").append(x + barWidth / 2).append("\" y=\"").append(height - bottom + 18).append("\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"10\">").append(c).append("</text>\n");
        }
        svg.append("<text x=\"500\" y=\"400\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"12\">Cutoff (orange bars mark the course reference values 7 and 10; lower is better)</text></svg>\n");
        Files.write(path, svg.toString().getBytes("UTF-8"));
    }

    private static String join(int[] values) { StringBuilder s = new StringBuilder(); for (int i=0;i<values.length;i++) { if(i>0)s.append(", "); s.append(values[i]); } return s.toString(); }
    private static String join(String[] values) { StringBuilder s = new StringBuilder(); for (int i=0;i<values.length;i++) { if(i>0)s.append(", "); s.append(values[i]); } return s.toString(); }
}
