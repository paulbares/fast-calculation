package me.paulbares.medium;

import generator.CsvGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static me.paulbares.BenchmarkRunner.INSTANCE;

public class Step5 {

    static {
        System.out.println("Process " + ProcessHandle.current().pid());
    }

    public static void main(String[] args) throws Exception {
        List<AggregateResult> results = new ArrayList<>();
        INSTANCE.run(() -> {
            FileInputStream fileInputStream = new FileInputStream(CsvGenerator.FILE_PATH);
            MyConsumer consumer = new MyConsumer() {
                @Override
                public AggregateResult createResult() {
                    return new AggregateResultBranchless();
                }
            };
            Step4.read(fileInputStream, consumer);
            results.add(consumer.result);
        });

        System.out.println(results.get(0).buildResult());
    }
}
