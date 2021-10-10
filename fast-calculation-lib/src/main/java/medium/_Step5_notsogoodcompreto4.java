package medium;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import generator.CsvGenerator;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class _Step5_notsogoodcompreto4 {
    public static void main(String[] args) throws Exception {
        File file = new File(CsvGenerator.FILE_PATH);

        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            Iterator<String> iterator = Files.lines(file.toPath()).iterator();
            AggregateResult result = new AggregateResult();
            CharString[] buffer = new CharString[3];
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = new CharString();
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

    private static final void fillBuffer(String line, CharString[] buffer) {
        int index = 0;
        int prev = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ',') {
                if (index < 3) { // numbers are the three first columns
                    buffer[index++].withUnderlying(line).withOffset(prev).withLength(i - prev);
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

    public static class CharString implements CharSequence {

        private String underlying;

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
            return this.underlying.charAt(this.offset + index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new RuntimeException("Not implemented"); // we do not need this
        }

        @Override
        public String toString() {
            // we have to implement this method because FastDoubleParser#parseRestOfDecimalFloatLiteralTheHardWay
            return this.underlying.substring(this.offset, this.offset + this.length);
        }

        public CharString withUnderlying(String underlying) {
            this.underlying = underlying;
            return this;
        }

        public CharString withOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public CharString withLength(int length) {
            this.length = length;
            return this;
        }
    }
}
