import ch.randelshofer.fastdoubleparser.FastDoubleParser;

import java.util.Arrays;

class AggregateResult {

    int[] sum = new int[2];
    int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
    double sumPrice = 0;
    double minPrice = 0;
    int count = 0;

    public void aggregate(CharSequence[] row) {
        int year = Integer.parseInt(row[0], 0, row[0].length(), 10);
        int mileage = Integer.parseInt(row[1], 0, row[1].length(), 10);
        double price = FastDoubleParser.parseDouble(row[2]);

        // branchless
        int t = (year - 2005) >> 31;
        int i = ~t; // if year is >= 2005, i is -1 i.e all 1 bits, zero otherwise
        this.sum[0] += i & year;
        this.sum[1] += i & mileage;
        this.min[0] = 2005; // easy!!
        this.min[1] = Math.min(this.min[1], (t >>> 1) | mileage); // little trick. arg = if a >= 128 { b
        // } else { Integer.MAX_VALUE }. Work if mileage is positive only
        this.sumPrice += Double.longBitsToDouble(((long) i) & Double.doubleToRawLongBits(price));
        this.minPrice = Math.min(this.minPrice, Double.longBitsToDouble((long) (t >>> 1) | (long) price));
        this.count += i & 1;
    }

    void merge(AggregateResult r2) {
        this.sum[0] += r2.sum[0];
        this.sum[1] += r2.sum[1];
        this.count += r2.count;
        this.sumPrice += r2.sumPrice;
        this.minPrice = Math.min(this.minPrice, r2.minPrice);

        this.min[0] = Math.min(this.min[0], r2.min[0]);
        this.min[1] = Math.min(this.min[1], r2.min[1]);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getSimpleName())
                .append("{").append("sum=")
                .append(Arrays.toString(sum))
                .append(", min=")
                .append(Arrays.toString(min))
                .append(", sumPrice=")
                .append(sumPrice)
                .append(", minPrice=")
                .append(minPrice)
                .append(", count=")
                .append(count)
                .append('}').toString();
    }
}
