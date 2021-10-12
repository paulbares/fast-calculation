package medium;

import generator.CsvGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static me.paulbares.BenchmarkRunner.SINGLE;

public class Step7Copy {

    static {
        System.out.println("Process " + ProcessHandle.current().pid());
    }

    static final int capacity = 1 << 16; // good perf that value

    public static void main(String[] args) throws Exception {
        List<Step5.AggregateResult> results = new ArrayList<>();
        SINGLE.run(() -> {
            Step5.MyConsumer consumer = new Step5.MyConsumer();
            read(5, 2048, consumer);
            read(2049, 3500, consumer);
            read(3501, 4000, consumer);
//            results.add(consumer.result);
//            long targetBatchSize = 127;
//            long length = new File(CsvGenerator.FILE_PATH).length();
//
//            ParsingTask parEach = new ParsingTask(null, targetBatchSize, 0, length);
//            parEach.invoke();
        });

//        System.out.println(results.get(0).buildResult());
    }

    private static void read(
            long startPosition,
            long endPosition,
            Step5.Consumer consumer) throws IOException {
        System.out.println("-----------------");
        FileInputStream fileInputStream = new FileInputStream(CsvGenerator.FILE_PATH);
        FileChannel fileChannel = fileInputStream.getChannel();
        int prefetch = 1024; // we prefetch 1024 bytes
        long startPrefetch = Math.max(0, startPosition - prefetch);
        fileChannel.position(startPrefetch);// set the position but we will advance to the beginning of the next line if
        int capacity = (int) (endPosition - startPrefetch);
        System.out.println("Read from " + startPosition + " to " + endPosition + ", prefetching from " + startPrefetch + ", capacity " + capacity);
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity); // TODO make sure capacity is enough // FIXME should be power of 2

        int i = 0;
        byteBuffer.clear();
        if (fileChannel.read(byteBuffer, 0) <= 0) { // FIXME might be a big big buffer
            return;
        }

        // Position the cursor
        // Move the end of the buffer to the end of the previous line if not at the end of a line
        byte[] aBuf = byteBuffer.array();
        int limit = aBuf.length - 1;
        // move to the previous line
        for (int j = aBuf.length - 1; j >= 0; j--) {
            if (aBuf[j] == '\n') {
                // eol previous line found
                limit = j; // FIXME handle when not found
                break;
            }
        }

        // Move the start to the beginning of the line
        int startIndex = -1;
        for (int j = capacity - (int) (endPosition - startPosition); j >= 0; j--) {
            if (aBuf[j] == '\n') {
                startIndex = j + 1;
                break;
            }
        }

        if (startIndex == -1) { // not found
            if (startPosition <= prefetch) {
                // begin of file. it is normal
                startIndex = 0;
            } else {
                // Bad prefetch (not enough), Redo with more
                throw new RuntimeException("Incorrect prefetch");
            }
        }

        System.out.println("StartIndex = " + startIndex);
        System.out.println("Reading file from bytes: " + (startPrefetch + startIndex) + " to " + (startPrefetch + startIndex + limit));

        int lines = 0;
        byteBuffer.flip().limit(limit);
        StringBuilder sb = new StringBuilder();
        char[] valueBuffer = new char[1 << 5]; // We use this size. It is big enough
        for (int j = 0; j < limit; j++) {
            char c = (char) byteBuffer.get();
            if (j < startIndex) {
                continue;
            }

            char b = (valueBuffer[i++] = c);
//            System.out.print(b);
            sb.append(b);
            boolean eol = b == '\n' || b == '\r';
            if (b == ',' || eol) {
                consumer.accept(valueBuffer, i - 1); // minus 1 for the comma
                if (eol) {
                    lines++;
                    if (lines == 1) {
                        System.out.println("First line = "+ sb.subSequence(0, sb.length() - 1));
                    }
                    sb = new StringBuilder();
                    consumer.eol();
                }
                i = 0;
            }
        }
        System.out.println("Last line = "+ sb.subSequence(0, sb.length()));
        System.out.println("Number of lines read = " + lines);
//        System.out.println("==============");

        fileChannel.close();
    }

    static class ParsingTask extends CountedCompleter<Step5.AggregateResult> {

        private static final AtomicInteger ID = new AtomicInteger();
        private static final AtomicLong OFFSET = new AtomicLong();

        final long targetBatchSize;
        final Step5.AggregateResult result = new Step5.AggregateResult();
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
                System.out.println(String.format("Fork Parent %s; child %s, l=%s, h=%s", this, task, newLow, h));
                this.children.add(task);
                h = newLow;
                task.fork();
            }

            // TODO do the job and fill AggregateResult
            System.out.println("Compute " + this + " from " + l + " to " + h);

            tryComplete();
        }

        @Override
        public Step5.AggregateResult getRawResult() {
            return this.result;
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            for (ParsingTask child : this.children) {
//                System.out.println(String.format("Parent %s = %s; child %s = %s", this, this.result.count, child.toString(), child.result.count));
                this.result.merge(child.result);
            }
        }

        @Override
        public String toString() {
            return "#" + id;
        }
    }
}
