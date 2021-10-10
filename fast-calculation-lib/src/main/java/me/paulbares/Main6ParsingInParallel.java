package me.paulbares;

import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Stream;

/**
 * Continuation of {@link Main5Branchless}.
 * TODO check what can be done with {@link java.nio.file.Files#lines(Path)} {@link Stream#parallel()}...
 *
 * Failed. It is way too long!!
 */
public class Main6ParsingInParallel {

    // expected result:
    // me.paulbares.AggregateResult{sum=[124751138, -1552455681], min=[2005, 1000], sumPrice=2.187283345427398E11, minPrice=0.0, count=4330277}
    public static void main(String[] args) throws Exception {
        File file = new File(CsvGenerator.FILE_PATH);
        long count = Files.lines(file.toPath()).count();

        List<AggregateResult> results = new ArrayList<>();
        BenchmarkRunner.INSTANCE.run(() -> {
            ParserTask task = new ParserTask(CsvGenerator.FILE_PATH, 0, count, null);
            ForkJoinPool.commonPool().invoke(task);
            results.add(task.aggregate);
        });
        System.out.println(results);
    }

    static class ParserTask extends RecursiveAction {

        final String file;
        final long start;
        final long end;
        final AggregateResult aggregate;
        final ParserTask parserTask;

        ParserTask(String file, long start, long end, ParserTask parserTask) {
            this.file = file;
            this.start = start;
            this.end = end;
            this.aggregate = new AggregateResult();
            this.parserTask = parserTask;
        }

        protected void computeAggregateResult(long start, long end) {
//            CsvParserSettings settings = new CsvParserSettings();
//            settings.setNumberOfRowsToSkip(start);
//            settings.setNumberOfRecordsToRead(end - start);
//            settings.setReadInputOnSeparateThread(false);
//            CsvParser parser = new CsvParser(settings);
//            parser.beginParsing(new File(this.file));
//
            try {
//                System.out.println("Reading file from line " + start + " to line " + end);
                Stream<String> lines = Files.lines(new File(this.file).toPath());
                lines
//                        .skip(start)
//                        .limit(end - start)
                        .forEach(
                                line -> {
                                    String[] split = line.split(",");
                                }
                        );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//
//            String[] row;
//            while ((row = parser.parseNext()) != null) {
//                this.aggregate.aggregate(row);
//            }
//            parser.stopParsing();
        }

        @Override
        protected void compute() {
            long l = this.start;
            long h = this.end;
//            ParserTask right = null;
//            while (h - l > 1000_000 && getSurplusQueuedTaskCount() <= 3) {
//                long mid = (l + h) >>> 1;
//                right = new ParserTask(this.file, mid, h, right);
//                right.fork();
//                h = mid;
//            }
            computeAggregateResult(l, h);

//            while (right != null) {
//                if (right.tryUnfork()) {
//                    // directly calculate if not stolen
//                    right.computeAggregateResult(right.start, right.end);
//                } else {
//                    right.join();
//                }
//                merge(right.aggregate);
//                right = right.parserTask;
//            }
        }
    }

}
