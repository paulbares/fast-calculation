package me.paulbares.medium;

import generator.CsvGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step4 {

    static {
        System.out.println("Process " + ProcessHandle.current().pid());
    }

    static final int capacity = 1 << 22; // good perf that value

    public static void main(String[] args) throws Exception {
        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            FileInputStream fileInputStream = new FileInputStream(CsvGenerator.FILE_PATH);
            MyConsumer consumer = new MyConsumer() {
                @Override
                public AggregateResult createResult() {
                    return new AggregateResultBranching();
                }
            };
            read(fileInputStream, consumer);
            results.add(consumer.result);
        });

        System.out.println(results.get(0).buildResult());
    }

    static void read(FileInputStream fileInputStream, Consumer consumer) throws IOException {
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

        char[] valueBuffer = new char[1 << 5]; // We use this size. It is big enough
        int read;
        int index = 0;
        int i = 0;
        do {
            byteBuffer.clear();
            if ((read = fileChannel.read(byteBuffer, index * capacity)) <= 0) {
                return;
            }

            int limit = byteBuffer.flip().limit();
            while (limit > 0) {
                char b = (valueBuffer[i++] = (char) byteBuffer.get());
                boolean eol = b == '\n';
                if (b == ',' || eol) {
                    consumer.accept(valueBuffer, i - 1); // minus 1 for the comma
                    if (eol) {
                        consumer.eol();
                    }
                    i = 0;
                }
                limit--;
            }
            index++;
        } while (read > 0);

        fileChannel.close();
    }
}
