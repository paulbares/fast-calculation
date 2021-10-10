package me.paulbares;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

public class ReadZipFile {

    private static final String name = "/Users/paul/dev/github/fast-calculation/fast-calculation-lib/src/main/resources/ford_1.5GB.csv.zip";

    static public void main(String args[]) throws Exception {
        BenchmarkRunner.INSTANCE.run(() -> {
            read1();
        });
    }

    static final int capacity = 1 << 16; // good perf that value

    static {
        System.out.println(capacity);
    }

    private static void read1() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(name);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        byte[] buffer = new byte[capacity];

        zipInputStream.getNextEntry();
        int bytesRead;
        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            for (int i = 0; i < buffer.length && i < bytesRead; i++) {
                char c = (char) buffer[i];
//                System.out.print(c);
            }
        }
        zipInputStream.close();
    }
}
