package me.paulbares.medium;

import generator.CsvGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step6 {

    static {
        System.out.println("Process " + ProcessHandle.current().pid());
    }

    static final int capacity = 1 << 18; // good perf that value

    public static void main(String[] args) throws Exception {
        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            long length = CsvGenerator.FILE.length();

//            int step = 1 << 26;
            long step = length / (ForkJoinPool.getCommonPoolParallelism() * 4); // batch size in bytes

//            MyConsumer consumer = new MyConsumer();
//            for (int i = 0; i < length - step; i += step) {
//                System.out.println(String.format("from %s to %s", i, i + step));
//                read(i, i + step, consumer);
//            }
//
//            long last = step * (length / step);
//            System.out.println(String.format("from %s to %s", last, last + (length % step)));
//            read(last, last + (length % step), consumer);

            ParsingTask parEach = new ParsingTask(null, step, 0, length);
            AggregateResult r = parEach.invoke();
            results.add(r);
        });

        System.out.println(results.get(0).buildResult());
    }

    private static void read(
            long startPosition,
            long endPosition,
            Consumer consumer) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(CsvGenerator.FILE);
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

        char[] valueBuffer = new char[1 << 5]; // We use this size. It is big enough
        int read;
        int index = 0;
        int i = 0;
        boolean startConsume = startPosition == 0; // for beginning of the file, we start right away
        boolean endConsume = false;
        int totalRead = 0;
        do {
            byteBuffer.clear();
            if ((read = fileChannel.read(byteBuffer, startPosition + index * capacity)) <= 0) {
                return;
            }

            int limit = byteBuffer.flip().limit();
            while (limit > 0) {
                char b = (char) byteBuffer.get();
                totalRead++;
                boolean eol = b == '\n';
                if (!startConsume) {
                    if (eol) {
                        startConsume = true; // new iteration, the beginning of the new line will be read.
//                        System.out.println("Consumption starts after reading " + totalRead + " bytes. Position:" + (startPosition + totalRead));
                    }
                    limit--;
                    continue;
                }

                if (totalRead > endPosition - startPosition) {
                    // we have read all the required bytes. From now on, we look for the first eol and then we stop reading
                    endConsume = true;
                }

                valueBuffer[i++] = b;
//                System.out.print(b);
                if (b == ',' || eol) {
                    consumer.accept(valueBuffer, i - 1); // minus 1 for the comma
                    if (eol) {
                        consumer.eol();
                    }
                    i = 0;
                }
                limit--;

                if (endConsume && eol) {
//                    System.out.println("Consumption stop after reading " + totalRead + " bytes. Position:" + (startPosition + totalRead));
                    return; // stop reading
                }
            }
            index++;
        } while (read > 0);

        fileChannel.close();
    }

    static class ParsingTask extends CountedCompleter<AggregateResult> {

        private static final AtomicInteger ID = new AtomicInteger();

        final long targetBatchSize;
        final Consumer consumer = new MyConsumer() {
            @Override
            public AggregateResult createResult() {
                return new AggregateResultBranchless();
            }
        };
        final int id = ID.getAndIncrement();
        final List<ParsingTask> children = new ArrayList<>();
        final long low;
        final long high;

        ParsingTask(ParsingTask parent, long targetBatchSize, long low, long high) {
            super(parent);
            this.targetBatchSize = targetBatchSize;
            this.low = low;
            this.high = high;
        }

        public void compute() {
            long l = low;
            long h = high;
            while ((h - l) > this.targetBatchSize) {
                long mid = (h - l) >>> 1;
                addToPendingCount(1);
                long newLow = l + mid;
                ParsingTask task = new ParsingTask(this, this.targetBatchSize, newLow, h);
//                System.out.println(String.format("Fork Parent %s; child %s, l=%s, h=%s", this, task, newLow, h));
                this.children.add(task);
                h = newLow;
                task.fork();
            }

            // TODO do the job and fill AggregateResult
//            System.out.println("Compute " + this + " from " + l + " to " + h);
            try {
                read(l, h, this.consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            tryComplete();
        }

        @Override
        public AggregateResult getRawResult() {
            return this.consumer.getResult();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            for (ParsingTask child : this.children) {
//                System.out.println(String.format("Parent %s = %s; child %s = %s", this, this.result.count, child.toString(), child.result.count));
                this.consumer.getResult().merge(child.consumer.getResult());
            }
        }

        @Override
        public String toString() {
            return "#" + id;
        }
    }
}
