package me.paulbares.medium;

public class AggregateResultBranching extends AAggregateResult {

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
    public void merge(AggregateResult result) {
        throw new RuntimeException("not implemented");
    }
}
