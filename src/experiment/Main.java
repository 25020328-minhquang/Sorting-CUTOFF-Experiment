package experiment;

import java.io.IOException;
import java.nio.file.Paths;

public final class Main {
    private Main() { }
    public static void main(String[] args) throws IOException {
        ExperimentRunner.run(args.length == 0 ? Paths.get(".").toAbsolutePath().normalize() : Paths.get(args[0]).toAbsolutePath().normalize());
    }
}