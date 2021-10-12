package me.paulbares.medium;

public class AggregateResultBranching implements AggregateResult {

    final int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
    final int[] max = new int[]{0, 0};
    int sumMileage = 0;
    double sumPrice = 0;
    double minPrice = Double.MAX_VALUE;
    double maxPrice = 0;
    int count = 0;

    @Override
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

    @Override
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

    @Override
    public void merge(AggregateResult result) {
        throw new RuntimeException("not implemented");
    }
}
