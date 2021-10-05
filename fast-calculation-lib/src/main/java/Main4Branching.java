import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Same as Main3 but with if condition
 */
public class Main4Branching {

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

                if (year >= 2005) {
                  result.sum[0] += year;
                  result.sum[1] += mileage;
                  result.min[0] = Math.min(result.min[0], year);
                  result.min[1] = Math.min(result.min[1], mileage);
                  result.sumPrice += price;
                  result.minPrice = Math.min(result.minPrice, price);

                  result.count++;
                }
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
