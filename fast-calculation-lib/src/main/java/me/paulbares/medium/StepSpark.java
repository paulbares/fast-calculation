package me.paulbares.medium;

import generator.CsvGenerator;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import static me.paulbares.BenchmarkRunner.INSTANCE;
import static me.paulbares.BenchmarkRunner.SINGLE;
import static org.apache.spark.sql.functions.avg;
import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;

/**
 * Add this parameter to the JVM: {@code --add-exports java.base/sun.nio.ch=ALL-UNNAMED}
 */
public class StepSpark {

  public static void main(String[] args) throws Exception {
    long[] sum = new long[1];
    SparkSession spark = SparkSession
            .builder()
            .appName("Java Spark SQL Example")
            .config("spark.master", "local")
            .getOrCreate();

    StructType schema = new StructType()
            .add("year", "int")
            .add("mileage", "int")
            .add("price", "double")
            .add("brand", "string")
            .add("transmission", "string")
            .add("fuel type", "string");

    Dataset<Row> df = spark.read()
            .schema(schema)
            .option("charset", "US-ASCII")
            .option("delimiter", ",")
            .csv(CsvGenerator.FILE_PATH);
    
    SINGLE.run(() -> {
      Dataset<Row> agg = df.select("year", "mileage", "price")
              .filter(new Column("year").$greater$eq(2005))
              .agg(
                      min("year"),
                      min("mileage"),
                      min("price"),
                      max("year"),
                      max("mileage"),
                      max("price"),
                      avg("mileage"),
                      avg("price"));
//      agg.show();
      int anInt = agg.collectAsList().get(0).getInt(0);
      sum[0] += anInt;
    });
  }
}
