package medium;

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
            AggregateResult result = new AggregateResult();
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

    public static class AggregateResult {

        final int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        final int[] max = new int[]{0, 0};
        long sumMileage = 0;
        double sumPrice = 0;
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0;
        int count = 0;

        public void aggregate(int year, int mileage, double price) {
            if (year >= 2005) {
                min[0] = Math.min(min[0], year);
                max[0] = Math.max(min[0], year);
                min[1] = Math.min(min[1], mileage);
                max[1] = Math.max(max[1], mileage);
                minPrice = Math.min(minPrice, price);
                maxPrice = Math.max(maxPrice, price);
                sumMileage += mileage;
                sumPrice += price;
                count++;
            }
        }

        public String buildResult() {
            StringBuilder sb = new StringBuilder();
            sb.append("AggregateResult: ")
                    .append("avg(mileage)=").append((double) sumMileage / count).append("; ")
                    .append("avg(price)=").append(sumPrice / count).append("; ")
                    .append("min(year)=").append(min[0]).append("; ")
                    .append("max(year)=").append(max[0]).append("; ")
                    .append("min(mileage)=").append(min[1]).append("; ")
                    .append("max(mileage)=").append(max[1]).append("; ")
                    .append("min(price)=").append(minPrice).append("; ")
                    .append("max(price)=").append(maxPrice).append("; ");
            return sb.toString();
        }
    }
}
