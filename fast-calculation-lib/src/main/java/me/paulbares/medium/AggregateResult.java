package me.paulbares.medium;

public interface AggregateResult {
    void aggregate(int year, int mileage, double price);

    String buildResult();

    void merge(AggregateResult result);
}
