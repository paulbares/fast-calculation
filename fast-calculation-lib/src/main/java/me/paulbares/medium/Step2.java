package me.paulbares.medium;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step2 {
    public static void main(String[] args) throws Exception {
        File file = new File(CsvGenerator.FILE_PATH);

        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            Iterator<String> iterator = Files.lines(file.toPath()).iterator();
            AggregateResult result = new AggregateResultBranching();
            while (iterator.hasNext()) {
                String line = iterator.next();
                String[] values = line.split(",");
                int year = Integer.parseInt(values[0]);
                int mileage = Integer.parseInt(values[1]);
//                double price = TypeFormat.parseDouble(values[2]);
                double price = FastDoubleParser.parseDouble(values[2]);

                result.aggregate(year, mileage, price);
            }
            results.add(result);
        });

        System.out.println(results.get(0).buildResult());
    }
}
