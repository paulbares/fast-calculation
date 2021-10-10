package me.paulbares;

import com.google.common.base.Splitter;
import com.univocity.parsers.common.input.EOFException;
import generator.CsvGenerator;
import javolution.text.CharArray;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Continuation of {@link Main6BisParsingInParallel} and remove usage of {@link Splitter} to reduce the amount
 * of strings allocated...
 */
public class Main6BisCustomParsing {

    // expected result:
    // me.paulbares.AggregateResult{sum=[124751138, -1552455681], min=[2005, 1000], sumPrice=2.187283345427398E11, minPrice=0.0, count=4330277}
    public static void main(String[] args) throws Exception {
        File file = new File(CsvGenerator.FILE_PATH);

        List<AggregateResult> results = new ArrayList<>();
        BenchmarkRunner.INSTANCE.run(() -> {
            Spliterator<String> spliterator = Files.lines(file.toPath()).spliterator();
            long targetBatchSize = spliterator.estimateSize() / (ForkJoinPool.getCommonPoolParallelism() * 1); // batch size in bytes
            ParsingTask parEach = new ParsingTask(null, spliterator, targetBatchSize);
//            System.out.println("Root " + parEach);
            AggregateResult result = parEach.invoke();
            results.add(result);
        });
        System.out.println(results);
        System.out.println(EOFException.count.get());
    }

    static class ParsingTask extends CountedCompleter<AggregateResult> {

        private static final AtomicInteger ID = new AtomicInteger();

        final Spliterator<String> spliterator;
        final long targetBatchSize;
        AggregateResult result;
        final int id = ID.getAndIncrement();
        final List<ParsingTask> children = new ArrayList<>();

        ParsingTask(ParsingTask parent, Spliterator<String> spliterator, long targetBatchSize) {
            super(parent);
            this.spliterator = spliterator;
            this.targetBatchSize = targetBatchSize;
        }

        public void compute() {
            Spliterator<String> sub;
            while (spliterator.estimateSize() > targetBatchSize && (sub = spliterator.trySplit()) != null) {
                addToPendingCount(1);
                ParsingTask task = new ParsingTask(this, sub, targetBatchSize);
                this.children.add(task);
//                System.out.println(String.format("Fork Parent %s; child %s", this, task));
                task.fork();
            }

            this.result = new AggregateResult();
            // FIXME
            Splitter on = Splitter.on(',');
//            String[] row = new String[6]; // FIXME might change
            CharArray[] buffer = new CharArray[3];
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = new CharArray();
            }

            spliterator.forEachRemaining(s -> {
                char[] chars = s.toCharArray();
                int index = 0;
                int prev = 0;
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] == ',') {
                        if (index < 3) { // numbers are the three first columns
                            buffer[index++].setArray(chars, prev, i - prev);
                            prev = i + 1;

                            if (index == 3) { // FULL
                                break;
                            }
                        }
                    }
                }

                this.result.aggregate(buffer);
            });
            tryComplete();
        }

        @Override
        public AggregateResult getRawResult() {
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
