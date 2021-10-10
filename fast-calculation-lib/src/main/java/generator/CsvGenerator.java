package generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class CsvGenerator {

    public static final String FILE_PATH = "/Users/paul/dev/github/fast-calculation/fast-calculation-lib/src/main/resources/ford_1.5GB.csv";

    /**
     * Writes a {@link String} into a file.
     *
     * @param path    The path to the file to write to.
     * @param content The string to write into the file.
     * @throws IOException if the write failed.
     */
    public static void writeFile(String path, String content) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(path, true))) {
            out.print(content);
            out.flush();
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File(FILE_PATH);
        Random rand = new Random();
        for (int i = 0; i < 4096; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 32768; j++) {
//                sb
//                        .append(rand.nextInt(1990, 2021)) // year
//                        .append(',')
//                        .append(rand.nextInt(1000, 300_000)) // mileage
//                        .append(',')
//                        .append(rand.nextDouble(1000, 100_000)) // selling price
//                        .append(',')
//                        .append(BRAND[rand.nextInt(0, BRAND.length)]) // brand
//                        .append(',')
//                        .append(rand.nextBoolean() ? "M" : "A") // transmission
//                        .append(',')
//                        .append(rand.nextBoolean() ? "petrol" : "diesel") // fuel type
//                        .append(System.lineSeparator());
            }
            writeFile(file.getPath(), sb.toString());
        }
    }

    static final String[] BRAND = {
            "abarth",
            "Acura",
            "Alfa Romeo",
            "Aston Martin",
            "audi",
            "bentley",
            "BMW",
            "buick",
            "cadillac",
            "chevrolet",
            "chrysler",
            "Citroen",
            "dacia",
            "dodge",
            "Ferrari",
            "fiat",
            "ford",
            "GMC",
            "honda",
            "Hummer",
            "hyundai",
            "infiniti",
            "isuzu",
            "jaguar",
            "jeep",
            "kia",
            "Lamborghini",
            "Lancia",
            "Land Rover",
            "Lexus",
            "Lincoln",
            "lotus",
            "Maserati",
            "Mazda",
            "Mercedes Benz",
            "Mercury",
            "Mini van",
            "Mitsubishi",
            "Nissan",
            "Opel",
            "Peugeot",
            "Pontiac",
            "Porsche",
            "Ram",
            "Renault",
            "SAAB",
            "Saturn",
            "Scion",
            "Seat",
            "Skoda",
            "Smart",
            "subaru",
            "suzuki",
            "Toyota",
            "Volkswagen",
            "volvo"
    };
}
