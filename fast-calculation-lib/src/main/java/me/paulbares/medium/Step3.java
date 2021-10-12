package me.paulbares.medium;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import com.google.common.base.Splitter;
import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step3 {
    public static void main(String[] args) throws Exception {
        File file = new File(CsvGenerator.FILE_PATH);

        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            Iterator<String> iterator = Files.lines(file.toPath()).iterator();
            AggregateResult result = new AggregateResultBranching();
            Splitter splitter = Splitter.on(',');
            while (iterator.hasNext()) {
                String line = iterator.next();
                Iterator<String> split = splitter.split(line).iterator();
                int year = Integer.parseInt(split.next());
                int mileage = Integer.parseInt(split.next());
                double price = FastDoubleParser.parseDouble(split.next());

                result.aggregate(year, mileage, price);
            }
            results.add(result);
        });

        System.out.println(results.get(0).buildResult());
    }
}
