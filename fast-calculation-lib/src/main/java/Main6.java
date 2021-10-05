import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Continuation of {@link Main5Branchless}.
 */
public class Main6 {

  public static void main(String[] args) throws Exception {
    List<Result> results = new ArrayList<>();
    BenchmarkRunner.INSTANCE.run(() -> {
              Result result = new Result();
              File file = new File("/Users/Paul.Bares/github/fast-calculation/fast-calculation-lib/src/main" +
                      "/resources/ford.csv");

              CsvParserSettings settings = new CsvParserSettings();
              CsvParser parser = new CsvParser(settings);
              parser.beginParsing(file);

              String[] row;
              while ((row = parser.parseNext()) != null) {
                int year = Integer.parseInt(row[0]);
                int mileage = Integer.parseInt(row[1]);
                double price = FastDoubleParser.parseDouble(row[2]);

                // branchless
                int t = (year - 2005) >> 31;
                int i = ~t; // if year is >= 2005, i is -1 i.e all 1 bits, zero otherwise
                result.sum[0] += i & year;
                result.sum[1] += i & mileage;
                result.min[0] = 2005; // easy!!
                result.min[1] = Math.min(result.min[1], (t >>> 1) | mileage); // little trick. arg = if a >= 128 { b
                // } else { Integer.MAX_VALUE }. Work if mileage is positive only
                result.sumPrice += Double.longBitsToDouble(((long) i) & Double.doubleToRawLongBits(price));
                result.minPrice = Math.min(result.minPrice, Double.longBitsToDouble((long) (t >>> 1) | (long) price));

                result.count += i & 1;
              }

              parser.stopParsing();
              results.add(result);
            }
    );

    System.out.println(results);
  }

  static class Result {
    int[] sum = new int[2];
    int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
    double sumPrice = 0;
    double minPrice = 0;
    int count = 0;

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
