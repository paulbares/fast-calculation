import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Similar perf. than {@link Main6BisParsingInParallel}.
 */
public class Main6ParsingInParallel2 {

    // expected result:
    // AggregateResult{sum=[124751138, -1552455681], min=[2005, 1000], sumPrice=2.187283345427398E11, minPrice=0.0, count=4330277}
    public static void main(String[] args) throws Exception {
        File file = new File(CsvGenerator.FILE_PATH);

        List<AggregateResult> results = new ArrayList<>();
        BenchmarkRunner.INSTANCE.run(() -> {
            Spliterator<String> spliterator = Files.lines(file.toPath()).spliterator();
            long targetBatchSize = spliterator.estimateSize() / (ForkJoinPool.getCommonPoolParallelism() * 1); // batch size in bytes
            ParserTask task = new ParserTask(CsvGenerator.FILE_PATH, null, spliterator, targetBatchSize);
            ForkJoinPool.commonPool().invoke(task);
            results.add(task.aggregate);
        });
        System.out.println(results);
    }

    static class ParserTask extends RecursiveAction {

        final String file;
        final AggregateResult aggregate;
        final ParserTask parserTask;
        final Spliterator<String> spliterator;
        final long targetBatchSize;

        ParserTask(String file,
                   ParserTask parserTask,
                   Spliterator<String> spliterator,
                   long targetBatchSize) {
            this.file = file;
            this.spliterator = spliterator;
            this.targetBatchSize = targetBatchSize;
            this.aggregate = new AggregateResult();
            this.parserTask = parserTask;
        }

        protected void computeAggregateResult() {
            CsvParserSettings settings = new CsvParserSettings();
            settings.setReadInputOnSeparateThread(false); // a little better. to confirm
            // Average test time: 1733.8076923076924ms
            CsvParser parser = new CsvParser(settings);
            spliterator.forEachRemaining(s -> {
                String[] row = parser.parseLine(s);
                this.aggregate.aggregate(row);
            });
        }

        @Override
        protected void compute() {
            ParserTask right = null;
            Spliterator<String> sub;
            while (spliterator.estimateSize() > targetBatchSize && (sub = spliterator.trySplit()) != null) {
                right = new ParserTask(this.file, right, sub, targetBatchSize);
                right.fork();
            }

            computeAggregateResult();

            while (right != null) {
                if (right.tryUnfork()) {
                    // directly calculate if not stolen
                    right.computeAggregateResult();
                } else {
                    right.join();
                }
                this.aggregate.merge(right.aggregate);
                right = right.parserTask;
            }
        }
    }

}
