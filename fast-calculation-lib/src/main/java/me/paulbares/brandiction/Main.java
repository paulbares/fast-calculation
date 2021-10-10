package me.paulbares.brandiction;

import me.paulbares.BenchmarkRunner;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Process " + ProcessHandle.current().pid());

        // Generate data
        int arraySize = 32768;
        int data[] = new int[arraySize];

        Random rnd = new Random(0);
        for (int c = 0; c < arraySize; ++c) {
            data[c] = rnd.nextInt() % 256;
        }

        // !!! With this, the next loop runs faster
        Arrays.sort(data);

        AtomicLong bigSum = new AtomicLong();
        BenchmarkRunner.INSTANCE.run(() -> {
            long sum = 0;
            for (int i = 0; i < 100000; ++i) {
                for (int c = 0; c < arraySize; ++c) {   // Primary loop
                    if (data[c] >= 128) {
                        sum += data[c];
                    }
                }
            }
            bigSum.addAndGet(sum);
        });
        System.out.println("sum = " + bigSum);
    }
}
