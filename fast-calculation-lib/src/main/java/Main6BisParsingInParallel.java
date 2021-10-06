import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Continuation of {@link Main5Branchless}.
 * TODO check what can be done with {@link Files#lines(Path)} {@link Stream#parallel()}...
 * <p>
 * Failed. It is way too long!!
 */
public class Main6BisParsingInParallel {

    // expected result:
    // AggregateResult{sum=[124751138, -1552455681], min=[2005, 1000], sumPrice=2.187283345427398E11, minPrice=0.0, count=4330277}
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

            CsvParserSettings settings = new CsvParserSettings();
            settings.setReadInputOnSeparateThread(false); // a little better. to confirm
            // Average test time: 1733.8076923076924ms
            CsvParser parser = new CsvParser(settings);
            this.result = new AggregateResult();
            spliterator.forEachRemaining(s -> {
                String[] row = parser.parseLine(s);
                this.result.aggregate(row);
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
