package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalDateFactory {
    private static final Pattern ISO_LOCAL_DATE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern ENGLISH_DATE = Pattern.compile("(\\d{2}-\\d{2}-\\d{4})");
    
    public static LocalDate parse(String date) {
        if (date.isEmpty()) {
            return null;
        } else {
            Matcher isoMatcher = ISO_LOCAL_DATE.matcher(date);
            Matcher engMatcher = ENGLISH_DATE.matcher(date);
            if (isoMatcher.find()) {
                return LocalDate.parse(isoMatcher.group());
            } else if (engMatcher.find()) {
                return LocalDate.parse(engMatcher.group(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } else {
                log.error("Unable to parse {} into a LocalDate", date);
                return null;
            }
        }    
    }
}