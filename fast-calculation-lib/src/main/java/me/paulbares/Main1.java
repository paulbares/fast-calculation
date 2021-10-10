package me.paulbares;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main1 {

    public static void main(String[] args) throws Exception {
        CsvMapper mapper = new CsvMapper();
        // important: we need "array wrapping" (see next section) here:
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        mapper.enable(CsvParser.Feature.ALLOW_COMMENTS);
        URL resource = Thread.currentThread().getContextClassLoader().getResource("ford_300MB.csv");
        File csvFile = new File(resource.toURI()); // or from String, URL etc

        List<Result> results = new ArrayList<>();
        BenchmarkRunner.INSTANCE.run(() -> {
            MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(csvFile);
            Result result = new Result();
            while (it.hasNext()) {
                String[] row = it.next();
                int year = Integer.parseInt(row[0]);
                int mileage = Integer.parseInt(row[1]);
                double price = Double.parseDouble(row[2]);

                result.sum[0] += year;
                result.sum[1] += mileage;
                result.min[0] = Math.min(result.min[0], year);
                result.min[1] = Math.min(result.min[1], mileage);
                result.sumPrice += price;
                result.minPrice = Math.min(result.minPrice, price);

                result.count++;
            }

            results.add(result);
        });

        System.out.println(results);
    }

    static class Result {
        long[] sum = new long[2];
        long[] min = new long[2];
        double sumPrice = 0;
        double minPrice = 0;
        long count = 0;

        @Override
        public String toString() {
            return "Result{" +
                    "sum=" + Arrays.toString(sum) +
                    ", min=" + Arrays.toString(min) +
                    ", sumPrice=" + sumPrice +
                    ", minPrice=" + minPrice +
                    ", count=" + count +
                    '}';
        }
    }
}
