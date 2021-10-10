package medium;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step6 {
    public static void main(String[] args) throws Exception {
        File file = new File(CsvGenerator.FILE_PATH);

        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            Iterator<String> iterator = Files.lines(file.toPath()).iterator();
            AggregateResult result = new AggregateResult();
            CharArray[] buffer = new CharArray[3];
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = new CharArray();
            }
            while (iterator.hasNext()) {
                String line = iterator.next();
                fillBuffer(line, buffer);
                int year = Integer.parseInt(buffer[0], 0, buffer[0].length, 10);
                int mileage = Integer.parseInt(buffer[1], 0, buffer[1].length, 10);
                double price = FastDoubleParser.parseDouble(buffer[2]);

                result.aggregate(year, mileage, price);
            }
            results.add(result);
        });

        System.out.println(results.get(0).buildResult());
    }

    private static final void fillBuffer(String line, CharArray[] buffer) {
        int index = 0;
        int prev = 0;
        char[] chars = line.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ',') {
                if (index < 3) { // numbers are the three first columns
                    buffer[index++].withArray(chars).withOffset(prev).withLength(i - prev);
                    prev = i + 1;
                    if (index == 3) { // buffer is full, stop here
                        break;
                    }
                }
            }
        }
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
            int t = (year - 2005) >> 31;
            int tt = ~t; // if year is >= 2005, i is -1 i.e all 1 bits, zero otherwise

            min[0] = 2005;
            max[0] = Math.max(min[0], year);
            min[1] = Math.min(min[1], (t >>> 1) | mileage);
            int m = tt & mileage;
            max[1] = Math.max(max[1], m);
            long rawLong = Double.doubleToRawLongBits(price);
            minPrice = Math.min(minPrice, Double.longBitsToDouble((long) (t >>> 1) | rawLong));
            sumMileage += m;
            double p = Double.longBitsToDouble(((long) tt) & rawLong);
            maxPrice = Math.max(maxPrice, p);
            sumPrice += p;
            count += tt & 1;
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

    public static class CharArray implements CharSequence {


        /**
         * Holds the character array.
         */
        private char[] array;

        /**
         * Holds the index of the first character.
         */
        private int offset;

        /**
         * Holds the length of char sequence.
         */
        private int length;

        @Override
        public int length() {
            return this.length;
        }

        @Override
        public char charAt(int index) {
            // no boundary check to maximize the performance !
            return this.array[this.offset + index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new RuntimeException("Not implemented"); // we do not need this
        }


        public CharArray withArray(char[] array) {
            this.array = array;
            return this;
        }

        public CharArray withOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public CharArray withLength(int length) {
            this.length = length;
            return this;
        }

        @Override
        public String toString() {
            // we have to implement this method because FastDoubleParser#parseRestOfDecimalFloatLiteralTheHardWay
            return new String(this.array, this.offset, this.length);
        }
    }
}
