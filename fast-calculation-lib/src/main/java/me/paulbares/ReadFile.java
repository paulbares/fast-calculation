package me.paulbares;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Iterator;

public class ReadFile {

    private static final String name = "/Users/paul/dev/github/fast-calculation/fast-calculation-lib/src/main/resources/ford_1.5GB.csv";

    static public void main(String args[]) throws Exception {
        BenchmarkRunner.INSTANCE.run(() -> {
            read1();
//            read2();
        });
    }

    private static void read2() throws IOException {
        File file = new File(name);
        Iterator<String> iterator = Files.lines(file.toPath()).iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
    }

    static final int capacity = 1 << 16; // good perf that value
    static {
        System.out.println(capacity);
    }

    private static void read1() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(name);
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

        int read;
        int index = 0;
        do {
            read = read(fileChannel, byteBuffer, index * capacity);
            index++;
        } while (read > 0);

        fileChannel.close();
    }

    private static int read(FileChannel fileChannel, ByteBuffer byteBuffer, int startingPos) throws IOException {
        byteBuffer.clear();
        int read = fileChannel.read(byteBuffer, startingPos);
//        System.out.println(read + " bytes read");
        byteBuffer.flip();
        int limit = byteBuffer.limit();
        while (limit > 0) {
            byte b = byteBuffer.get();
//            System.out.print((char) b);
//            if(b == '\n') {
//                int j = 0;
//            }
            limit--;
        }
//        System.out.println();
        return read;
    }

}
