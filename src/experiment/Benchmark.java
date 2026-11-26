package experiment;

import java.util.Arrays;

public final class Benchmark {
    private Benchmark() { }

    public static final class Result {
        public final String algorithm;
        public final String distribution;
        public final int size;
        public final int cutoff;
        public final long medianNanos;
        public final int repetitions;

        public Result(String algorithm, String distribution, int size, int cutoff, long medianNanos, int repetitions) {
            this.algorithm = algorithm; this.distribution = distribution; this.size = size;
            this.cutoff = cutoff; this.medianNanos = medianNanos; this.repetitions = repetitions;
        }
    }

    public static Result measure(String algorithm, String distribution, int[] input, int cutoff, int warmups, int repetitions) {
        if (cutoff < 1) throw new IllegalArgumentException("cutoff must be at least 1");
        int[] expected = input.clone();
        Arrays.sort(expected);
        for (int i = 0; i < warmups; i++) sort(algorithm, input.clone(), cutoff);
        long[] times = new long[repetitions];
        for (int i = 0; i < repetitions; i++) {
            int[] copy = input.clone();
            long start = System.nanoTime();
            sort(algorithm, copy, cutoff);
            times[i] = System.nanoTime() - start;
            if (!Arrays.equals(copy, expected)) {
                throw new IllegalStateException(algorithm + " returned an incorrectly sorted array (cutoff=" + cutoff + ")");
            }
        }
        Arrays.sort(times);
        long median = times.length % 2 == 1 ? times[times.length / 2]
                : (times[times.length / 2 - 1] + times[times.length / 2]) / 2;
        return new Result(algorithm, distribution, input.length, cutoff, median, repetitions);
    }

    private static void sort(String algorithm, int[] values, int cutoff) {
        if ("MergeSort".equals(algorithm)) sorting.MergeSort.mergeSort(values, cutoff);
        else if ("QuickSort".equals(algorithm)) sorting.QuickSort.quickSort(values, cutoff);
        else throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
    }
}