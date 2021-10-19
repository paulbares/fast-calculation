package me.paulbares.medium;

public class AggregateResultBranchless extends AAggregateResult {

    @Override
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

    @Override
    public void merge(AggregateResult result) {
        AggregateResultBranchless r2 = (AggregateResultBranchless) result;
        this.sumMileage += r2.sumMileage;
        this.sumPrice += r2.sumPrice;
        this.count += r2.count;
        this.minPrice = Math.min(this.minPrice, r2.minPrice);
        this.maxPrice = Math.max(this.maxPrice, r2.maxPrice);

        this.min[0] = Math.min(this.min[0], r2.min[0]);
        this.min[1] = Math.min(this.min[1], r2.min[1]);
        this.max[0] = Math.max(this.max[0], r2.max[0]);
        this.max[1] = Math.max(this.max[1], r2.max[1]);
    }
}
