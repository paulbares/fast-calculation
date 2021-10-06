import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import generator.CsvGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Same as Main3 but with if condition
 *
 * It does not seem to change much. Maybe it is because it is way more costly to parse the strings than the rest.
 * Plus the result for price is false.
 *
 * branchless:
 * Average test time: 1238.1923076923076ms
 * Result{sum=[124879230, 1349871553], min=[2005, 1000], sumPrice=2.1870514021047272E11, minPrice=0.0, count=4330352}
 *
 * branching:
 * Average test time: 1347.576923076923ms
 * Result{sum=[124879230, 1349871553], min=[2005, 1000], sumPrice=2.1870514021047272E11, minPrice=0.0, count=4330352}
 */
public class Main5Branchless {

  public static void main(String[] args) throws Exception {
    List<AggregateResult> results = new ArrayList<>();
    BenchmarkRunner.INSTANCE.run(() -> {
              AggregateResult result = new AggregateResult();
              File file = new File(CsvGenerator.FILE_PATH);

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
}
