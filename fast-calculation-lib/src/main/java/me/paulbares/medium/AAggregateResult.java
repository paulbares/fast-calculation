package me.paulbares.medium;

public abstract class AAggregateResult implements AggregateResult {

    final int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
    final int[] max = new int[]{0, 0};
    long sumMileage = 0;
    double sumPrice = 0;
    double minPrice = Double.MAX_VALUE;
    double maxPrice = 0;
    int count = 0;

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
}
