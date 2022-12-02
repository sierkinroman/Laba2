package dev.profitsoft.intern.task2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dev.profitsoft.intern.task2.model.Fine;
import dev.profitsoft.intern.task2.model.FineType;
import dev.profitsoft.intern.task2.model.FinesStatistic;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task2 {

    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final String finesRootDirectory = "." + File.separator + "fines" + File.separator;

    public static void main(String[] args) throws IOException {
        FinesGenerator.createFines(finesRootDirectory);

        List<Fine> fineStatistic = getFineStatistic();

        fineStatistic.sort(Comparator.comparing(Fine::getFineAmount).reversed());

        writeStatisticToXml(fineStatistic);
    }

    public static List<Fine> getFineStatistic() throws IOException {
        Map<FineType, BigDecimal> finesAmountByType = new HashMap<>();

        for (File fineFile : getFineFiles(new File(finesRootDirectory))) {
            JsonParser jsonParser = jsonFactory.createParser(fineFile);
            checkCorrectedStartFile(jsonParser);

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                Fine fine = readFine(jsonParser);
                finesAmountByType.merge(fine.getType(), fine.getFineAmount(), BigDecimal::add);
            }
        }

        List<Fine> finesStatistics = new ArrayList<>();
        finesAmountByType.forEach((fineType, fineAmount) ->
                finesStatistics.add(new Fine(fineType, fineAmount)));

        return finesStatistics;
    }

    public static File[] getFineFiles(File dir) {
        String end = "fines.json";
        return dir.listFiles(file ->
                !file.isDirectory() && file.getName().endsWith(end));
    }

    private static void checkCorrectedStartFile(JsonParser jsonParser) throws IOException {
        if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("File should start like {\"fines\":[");
        }
        jsonParser.nextToken();
        if (!"fines".equals(jsonParser.getCurrentName())) {
            throw new IllegalStateException("File should start like {\"fines\":[");
        }
        if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("File should start like {\"fines\":[");
        }
    }

    private static Fine readFine(JsonParser jsonParser) throws IOException {
        if (jsonParser.currentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Fine object should start from {");
        }

        Fine fine = new Fine();

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String property = jsonParser.getCurrentName();
            jsonParser.nextToken();

            switch (property) {
                case "date_time":
                    fine.setDateTime(
                            LocalDateTime.parse(jsonParser.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    break;
                case "first_name":
                    fine.setFirstName(jsonParser.getText());
                    break;
                case "last_name":
                    fine.setLastName(jsonParser.getText());
                    break;
                case "type":
                    fine.setType(FineType.valueOf(jsonParser.getText()));
                    break;
                case "fine_amount":
                    fine.setFineAmount(BigDecimal.valueOf(jsonParser.getDoubleValue()));
                    break;
            }
        }

        return fine;
    }

    public static void writeStatisticToXml(List<Fine> fineStatistic) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        xmlMapper.writeValue(new File("fines_statistic.xml"), new FinesStatistic(fineStatistic));
    }

}
