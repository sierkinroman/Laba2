package dev.profitsoft.intern.task2;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.profitsoft.intern.task2.model.Fine;
import dev.profitsoft.intern.task2.model.FineType;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

public class FinesGenerator {

    private static int yearFrom = 2010;
    private static final String[] firstNames = {"Ivan", "Petro", "Anton", "Alex", "Leo", "Tom"};
    private static final String[] lastNames = {"Ivanov", "Petrov", "Antonov", "Alexov", "Leonov", "Tomov"};
    private static final double[] fineAmounts = {340.0, 1000.0, 610.0, 17000.0, 510.0, 700.0};
    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper OBJECT_MAPPER;
    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.registerModule(new JavaTimeModule());

        OBJECT_MAPPER = mapper;
    }

    public static void createFines(String finesRootDirectory) throws IOException {
        File finesDirectory = new File(finesRootDirectory);
        if (!finesDirectory.exists()) {
            finesDirectory.mkdir();
            Random random = new Random();

            for (; yearFrom < LocalDateTime.now().getYear(); yearFrom++) {
                JsonGenerator jsonGenerator = jsonFactory.createGenerator(
                        new File(finesRootDirectory + yearFrom + "_fines.json"), JsonEncoding.UTF8);
                jsonGenerator.setCodec(OBJECT_MAPPER);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("fines");
                jsonGenerator.writeStartArray();

                for (int i = 0; i < 100; i++) {
                    Fine fine = new Fine();
                    fine.setDateTime(createRandomDateWithYear(yearFrom, random));
                    fine.setFirstName(firstNames[random.nextInt(firstNames.length)]);
                    fine.setLastName(lastNames[random.nextInt(lastNames.length)]);
                    int fineTypeIndex = random.nextInt(FineType.values().length);
                    fine.setType(FineType.values()[fineTypeIndex]);
                    fine.setFineAmount(BigDecimal.valueOf(fineAmounts[fineTypeIndex]));

                    jsonGenerator.writeObject(fine);
                }

                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
                jsonGenerator.close();
            }
        }
    }

    private static LocalDateTime createRandomDateWithYear(int year, Random random) {
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt( 60);
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

}

