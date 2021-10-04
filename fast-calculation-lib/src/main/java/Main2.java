import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Same as {@link Main1} but without jackson
 */
public class Main2 {

    public static void main(String[] args) throws Exception {

        List<Result> results = new ArrayList<>();
        BenchmarkRunner.INSTANCE.run(() -> {
                    Result result = new Result();
                    InputStream st = Thread.currentThread().getContextClassLoader().getResourceAsStream("ford_300MB.csv");
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(st))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] row = line.split(",");
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
                    }

                    results.add(result);
                }
        );

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
