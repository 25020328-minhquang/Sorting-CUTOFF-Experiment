package experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public final class DataGenerator {
    public static final String[] DISTRIBUTIONS = {"random", "sorted", "reverse", "nearly_sorted", "few_unique"};

    private DataGenerator() { }

    public static int[] generate(int size, String distribution, long seed) {
        if (size < 0) throw new IllegalArgumentException("size must be non-negative");
        Random random = new Random(seed);
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = "few_unique".equals(distribution) ? random.nextInt(16) : random.nextInt();
        }
        if ("sorted".equals(distribution) || "reverse".equals(distribution) || "nearly_sorted".equals(distribution)) {
            Arrays.sort(values);
            if ("reverse".equals(distribution)) {
                for (int left = 0, right = size - 1; left < right; left++, right--) {
                    int temp = values[left]; values[left] = values[right]; values[right] = temp;
                }
            } else if ("nearly_sorted".equals(distribution)) {
                int swaps = Math.max(1, size / 100);
                for (int i = 0; i < swaps && size > 1; i++) {
                    int a = random.nextInt(size), b = random.nextInt(size);
                    int temp = values[a]; values[a] = values[b]; values[b] = temp;
                }
            }
        } else if (!"random".equals(distribution) && !"few_unique".equals(distribution)) {
            throw new IllegalArgumentException("Unknown distribution: " + distribution);
        }
        return values;
    }

    public static void writeSampleData(Path directory) throws IOException {
        Files.createDirectories(directory);
        for (String distribution : DISTRIBUTIONS) {
            int[] values = generate(200, distribution, 20261126L);
            StringBuilder csv = new StringBuilder("index,value\n");
            for (int i = 0; i < values.length; i++) csv.append(i).append(',').append(values[i]).append('\n');
            Files.write(directory.resolve(distribution + "_200.csv"), csv.toString().getBytes("UTF-8"));
        }
    }
}