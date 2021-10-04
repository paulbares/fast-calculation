package generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class CsvGenerator {

    /**
     * Writes a {@link String} into a file.
     *
     * @param path    The path to the file to write to.
     * @param content The string to write into the file.
     * @throws IOException if the write failed.
     */
    public static void writeFile(String path, String content) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(path, true))) {
            out.print(content);
            out.flush();
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/paul/dev/github/fast-calculation/fast-calculation-lib/src/main/resources/ford.csv");
        Random rand = new Random();
        for (int i = 0; i < 256; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 32768; j++) {
                sb
                        .append(rand.nextInt(1990, 2021))
                        .append(',')
                        .append(rand.nextInt(1000, 200_000))
                        .append(',')
                        .append(rand.nextDouble(1000, 100_000))
                        .append(System.lineSeparator());
            }
            writeFile(file.getPath(), sb.toString());
        }
    }
}
