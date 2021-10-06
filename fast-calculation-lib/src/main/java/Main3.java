import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import generator.CsvGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Same as but with univocity parser. TODO retest with jackson. This class uses {@link FastDoubleParser} because parsing
 * double what what causing the slowness.
 * <p>
 * -XX:-CompactStrings does not change anything as suggested here https://github.com/wrandelshofer/FastDoubleParser
 */
public class Main3 {

  public static void main(String[] args) throws Exception {
    List<Result> results = new ArrayList<>();
    BenchmarkRunner.INSTANCE.run(() -> {
              Result result = new Result();
              File file = new File(CsvGenerator.FILE_PATH);

              CsvParserSettings settings = new CsvParserSettings();
              CsvParser parser = new CsvParser(settings);
              parser.beginParsing(file);

              String[] row;
              while ((row = parser.parseNext()) != null) {
                int year = Integer.parseInt(row[0]);
                int mileage = Integer.parseInt(row[1]);
                double price = Double.parseDouble(row[2]); // javolution is 1 sec better !! with the file
//                        300MB !!
//                double price = javolution.text.TypeFormat.parseDouble(row[2]); // FastDoubleParser is
//                        almost 1 sec better !! with the file 300MB !!
//                double price = FastDoubleParser.parseDouble(row[2]);

                result.sum[0] += year;
                result.sum[1] += mileage;
                result.min[0] = Math.min(result.min[0], year);
                result.min[1] = Math.min(result.min[1], mileage);
                result.sumPrice += price;
                result.minPrice = Math.min(result.minPrice, price);

                result.count++;
                result.count += row.length;
              }

              parser.stopParsing();
              results.add(result);
            }
    );

    System.out.println(results);
  }

  static class Result {
    long[] sum = new long[2];
    long[] min = new long[]{Long.MAX_VALUE, Long.MAX_VALUE};
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
