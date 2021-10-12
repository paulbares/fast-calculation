package me.paulbares.medium;

import generator.CsvGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step6 {

    static {
        System.out.println("Process " + ProcessHandle.current().pid());
    }

    static final int capacity = 1 << 16; // good perf that value

    public static void main(String[] args) throws Exception {
        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            MyConsumer consumer = new MyConsumer() {
                @Override
                public AggregateResult createResult() {
                    return new AggregateResultBranchless();
                }
            };
            read(consumer);
            results.add(consumer.result);
        });

        System.out.println(results.get(0).buildResult());
    }

    private static void read(Consumer consumer) throws IOException {
        final FileChannel channel = new FileInputStream(CsvGenerator.FILE_PATH).getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        byte[] byteBuffer = new byte[capacity];

        char[] valueBuffer = new char[1 << 5]; // We use this size. It is big enough
        int remaining = Integer.MAX_VALUE;
        int i = 0;
        do {
            int size = Math.min(byteBuffer.length, remaining);
            buffer.get(byteBuffer, 0, size);

            for (int k = 0; k < size; k++) {
                char b = (valueBuffer[i++] = (char) byteBuffer[k]);
                boolean eol = b == '\n' || b == '\r';
                if (b == ',' || eol) {
                    consumer.accept(valueBuffer, i - 1); // minus 1 for the comma
                    if (eol) {
                        consumer.eol();
                    }
                    i = 0;
                }
            }
        } while ((remaining = buffer.remaining()) > 0);

        channel.close();
    }
}
