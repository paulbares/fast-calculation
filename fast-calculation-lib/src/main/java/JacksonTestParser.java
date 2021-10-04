import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

import java.io.File;
import java.net.URL;

public class JacksonTestParser {

    public static void main(String[] args) throws Exception {
        CsvMapper mapper = new CsvMapper();
        // important: we need "array wrapping" (see next section) here:
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        mapper.enable(CsvParser.Feature.ALLOW_COMMENTS);
        URL resource = Thread.currentThread().getContextClassLoader().getResource("ford_escort.csv");
        File csvFile = new File(resource.toURI()); // or from String, URL etc

        MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(csvFile);
        while (it.hasNext()) {
            String[] row = it.next();
//            System.out.println(Arrays.toString(row));
            // and voila, column values in an array. Works with Lists as well

            // FIXME Parse row depending on the type
            int value = Integer.parseInt(row[0]);

            // FIXME compute on the fly?

            // FIXME compute only if value if positive (branch prediction)
            if (Integer.signum(value) >= 0) { // remove branch here
                // compute
            }
        }

//        {
//            System.out.println("Positive:");
//            int i = 9;
//            System.out.println("result:" + sign1(i));
//            System.out.println("result:" + sign2(i));
//        }
//        {
//            System.out.println("Negative:");
//            int i = -9;
//            System.out.println("result:" + sign1(i));
//            System.out.println("result:" + sign2(i));
//        }
        System.out.println("result:" + sign1(0));
        System.out.println("result:" + sign2(0));

    }

    /**
     * 1 if positive, 0 if negative or zero !! different than {@link #sign2(int)}
     * @param i
     * @return
     */
    static int sign1(int i) {
        return (-i) >>> 31;
    }

    /**
     * 1 if positive or zero, 0 if negative
     * @param i
     * @return
     */
    static int sign2(int i) {
        return (~(i >> 31)) >>> 31;
    }
}
