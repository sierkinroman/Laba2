package dev.profitsoft.intern.task1;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlParserWithReplacingAttributesSavingFormatting {

    /**
     * To find tag <persons> with whitespaces on the sides
     */
    private static final String personsStartTagRegex = "\\s*<persons>\\s*";

    /**
     * To find tag </persons> with whitespaces on the sides
     */
    private static final String personsEndTagRegex = "\\s*</persons>\\s*";

    /**
     * To find tag <person ... />
     * ... - is a list of attributes in the format: attribute\s*=\s*"value"\s*
     * And extract:
     *      attributeName with whitespaces (group1)
     *      attributeValue (group2)
     */
    private static final String personTagRegex = "\\s*<person\\s*((\\S+)\\s*=\\s*\"([^\"]*)\"\\s*)*/>";

    /**
     * To find attribute surname\s*=\s*"surnameValue"
     * And extract surnameValue (group1)
     */
    private static final String surnameAttributeRegex = "\\s?\\bsurname\\s*=\\s*\"([^\"]*)\"";

    /**
     * To find attribute name\s*=\s*"nameValue"
     * And extract:
     *      name\s*=\s* (group1)
     *      nameValue (group2)
     */
    private static final String nameAttributeRegex = "(\\bname\\s*=\\s*)\"([^\"]*)\"";

    private static final Pattern surnameAttributePattern = Pattern.compile(surnameAttributeRegex);

    private static final Pattern nameAttributePattern = Pattern.compile(nameAttributeRegex);

    private String currentRow = "";

    public static void main(String[] args) {
        new XmlParserWithReplacingAttributesSavingFormatting()
                .copyXmlFileWithCorrectedPersons("persons.xml");
        new XmlParserWithReplacingAttributesSavingFormatting()
                .copyXmlFileWithCorrectedPersons("persons_oneRow.xml");
    }

    public void copyXmlFileWithCorrectedPersons(String inFileName) {
        String outFileName = "processed_" + inFileName;
        try (InputStream is = new FileInputStream(inFileName);
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFileName));
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
//            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8);
            writeAllBeforePersons(scanner, bufferedWriter);
            writePersons(scanner, bufferedWriter);
            writeAllAfterPersons(scanner, bufferedWriter);
        } catch (FileNotFoundException e) {
            System.out.println(inFileName + " is not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAllBeforePersons(Scanner scanner, BufferedWriter bufferedWriter) throws IOException {
        Pattern personsStartTagPattern = Pattern.compile(personsStartTagRegex);
        while (scanner.hasNextLine()) {
            currentRow = nextLine(scanner);

            Matcher personsStartTagMatcher = personsStartTagPattern.matcher(currentRow);
            if (personsStartTagMatcher.find()) {
                bufferedWriter.write(currentRow.substring(0, personsStartTagMatcher.end()));
                currentRow = currentRow.substring(personsStartTagMatcher.end());
                break;
            } else {
                bufferedWriter.write(currentRow);
            }
        }
    }

    private String nextLine(Scanner scanner) {
        return scanner.nextLine() + System.lineSeparator();
    }

    private void writePersons(Scanner scanner, BufferedWriter bufferedWriter) throws IOException {
        Pattern personTagPattern = Pattern.compile(personTagRegex);
        Pattern personsEndTagPattern = Pattern.compile(personsEndTagRegex);

        do {
            Matcher personMatcher = personTagPattern.matcher(currentRow);
            while (personMatcher.find()) {
                String person = getCorrectedPerson(personMatcher.group());
                bufferedWriter.write(person);
                currentRow = currentRow.replaceFirst(personTagRegex, "");
            }

            Matcher personsEndTagMatcher = personsEndTagPattern.matcher(currentRow);
            if (personsEndTagMatcher.find()) {
                bufferedWriter.write(personsEndTagMatcher.group());
                currentRow = currentRow.substring(personsEndTagMatcher.end());
                break;
            }
            currentRow += nextLine(scanner);
        } while (scanner.hasNextLine());
    }

    private String getCorrectedPerson(String person) {
        String personWithCorrectName = combineNameWithSurname(person);
        return personWithCorrectName.replaceAll(surnameAttributeRegex, "");
    }

    private String combineNameWithSurname(String person) {
        Matcher nameMatcher = nameAttributePattern.matcher(person);
        String replacement = "";
        if (nameMatcher.find()) {
            replacement = String.format("%s\"%s %s\"",
                    nameMatcher.group(1),
                    getName(person),
                    getSurname(person));
        }
        return person.replaceAll(nameAttributeRegex, replacement);
    }

    private String getName(String person) {
        Matcher nameMatcher = nameAttributePattern.matcher(person);
        return nameMatcher.find() ? nameMatcher.group(2) : "";
    }

    private String getSurname(String person) {
        Matcher surnameMatcher = surnameAttributePattern.matcher(person);
        return surnameMatcher.find() ? surnameMatcher.group(1) : "";
    }

    private void writeAllAfterPersons(Scanner scanner, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(currentRow);
        while (scanner.hasNextLine()) {
            currentRow = nextLine(scanner);
            bufferedWriter.write(currentRow);
        }
    }

}
