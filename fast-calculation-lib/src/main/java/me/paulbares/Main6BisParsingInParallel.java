package me.paulbares;

import com.google.common.base.Splitter;
import com.univocity.parsers.common.input.EOFException;
import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Continuation of {@link Main5Branchless}.
 * This parse in parallel and remove also usage of univocity in favor of {@link Splitter}. because
 * {@link com.univocity.parsers.csv.CsvParser#parseLine(String)} throws a {@link EOFException} after reading a new line....
 * => So several millions !!!
 *
 *
 * Flight recording with Splitter shows that it allocates a lot , a lot of String:
 *
 *
 * Stack Trace	Count	Percentage
 * byte[] java.util.Arrays.copyOfRange(byte[], int, int)	2181	70.6 %
 * String java.lang.StringLatin1.newString(byte[], int, int)	2181	70.6 %
 * String java.lang.String.substring(int, int)	2172	70.3 %
 * CharSequence java.lang.String.subSequence(int, int)	2171	70.2 %
 * String com.google.common.base.Splitter$SplittingIterator.computeNext()	2171	70.2 %
 *
 * (58 GiB, 46.3 GiB, 150 MiB)
 * Class	Max Live Count	Max Live Size	Live Size Increase	Alloc Total	Total Allocation (%)	Alloc in TLABs	Alloc Outside TLABs
 * byte[]				6.2304101728E10 B	55.44948983362241 %
 * java.lang.String				4.9686681432E10 B	44.220220828445925 %
 * java.nio.HeapCharBuffer				1.57631584E8 B	0.14028917313701836 %
 *
 * Maybe we can do better !!!! and works only with byte[] array if we parse line ourself!!
 */
public class Main6BisParsingInParallel {

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
            Splitter on = Splitter.on(',');
            String[] row = new String[6]; // FIXME might change
            spliterator.forEachRemaining(s -> {
                Iterable<String> fields = on.split(s);
                int i = 0;
                for (String field : fields) {
                    row[i++] = field;
                }
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
