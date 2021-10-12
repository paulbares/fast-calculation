package me.paulbares.medium;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import generator.CsvGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step6 {

    static {
        System.out.println("Process " + ProcessHandle.current().pid());
    }

//    static final int capacity = 1 << 16; // good perf that value
    static final int capacity = 1 << 24; // good perf that value

    public static void main(String[] args) throws Exception {
        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            MyConsumer consumer = new MyConsumer();
            read(consumer);
            results.add(consumer.result);
        });

        System.out.println(results.get(0).buildResult());
    }

    private static void read(Consumer consumer) throws IOException {
        final FileChannel channel = new FileInputStream(CsvGenerator.FILE_PATH).getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        byte[] byteBuffer = new byte[capacity];

        char[] valueBuffer = new char[1 << 5]; // We use this size. It is big enough
        int remaining = Integer.MAX_VALUE;
        int i = 0;
        do {
            int size = Math.min(byteBuffer.length, remaining);
            buffer.get(byteBuffer, 0, size);

            for (int k = 0; k < size; k++) {
                char b = (valueBuffer[i++] = (char) byteBuffer[k]);
                boolean eol = b == '\n' || b == '\r';
                if (b == ',' || eol) {
                    consumer.accept(valueBuffer, i - 1); // minus 1 for the comma
                    if (eol) {
                        consumer.eol();
                    }
                    i = 0;
                }
            }
        } while ((remaining = buffer.remaining()) > 0);

        channel.close();
    }

    protected interface Consumer {
        void accept(char[] a, int length);

        void eol();
    }

    protected static final class MyConsumer implements Consumer {

        private int count;
        private final CharArray charArray = new CharArray();

        protected final AggregateResult result = new AggregateResult();

        private int year;
        private int mileage;
        private double price;

        @Override
        public void accept(char[] a, int length) {
            CharArray charSeq = charArray.withArray(a).withLength(length);
            if (count == 0) {
                year = Integer.parseInt(charSeq, 0, length, 10);
            } else if (count == 1) {
                mileage = Integer.parseInt(charSeq, 0, length, 10);
            } else if (count == 2) {
                price = FastDoubleParser.parseDouble(charSeq);
                result.aggregate(year, mileage, price);
            }
            count++;
        }

        @Override
        public void eol() {
            count = 0; // reset
        }
    }


    protected static class AggregateResult {

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

        public void merge(AggregateResult r2) {
            this.sumMileage += r2.sumMileage;
            this.sumPrice += r2.sumPrice;
            this.count += r2.count;
            this.sumPrice += r2.sumPrice;
            this.minPrice = Math.min(this.minPrice, r2.minPrice);
            this.maxPrice = Math.max(this.maxPrice, r2.maxPrice);

            this.min[0] = Math.min(this.min[0], r2.min[0]);
            this.min[1] = Math.min(this.min[1], r2.min[1]);
            this.max[0] = Math.max(this.max[0], r2.max[0]);
            this.max[1] = Math.max(this.max[1], r2.max[1]);
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
            return this.array[index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new RuntimeException("Not implemented"); // we do not need this
        }

        public CharArray withArray(char[] array) {
            this.array = array;
            return this;
        }

        public CharArray withLength(int length) {
            this.length = length;
            return this;
        }

        @Override
        public String toString() {
            // we have to implement this method because FastDoubleParser#parseRestOfDecimalFloatLiteralTheHardWay
            return new String(this.array, 0, this.length);
        }
    }
}
