package uk.ac.ceh.gateway.catalogue.gemini;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalDateFactory {
    private static final Pattern ISO_LOCAL_DATE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern ENGLISH_DATE = Pattern.compile("(\\d{2}-\\d{2}-\\d{4})");
    private static final Pattern ALL_NUMBERS_DATE = Pattern.compile("(\\d{8})");
    private static final Pattern ISO_LOCAL_DATE_TIME = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})");
    
    public static LocalDate parse(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        } else {
            Matcher iso = ISO_LOCAL_DATE.matcher(date);
            Matcher english = ENGLISH_DATE.matcher(date);
            Matcher allNumber = ALL_NUMBERS_DATE.matcher(date);
            if (iso.find()) {
                return LocalDate.parse(iso.group());
            } else if (english.find()) {
                return LocalDate.parse(english.group(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } else if (allNumber.find()) {
                return LocalDate.parse(allNumber.group(), DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                log.error("Unable to parse {} into a LocalDate", date);
                return null;
            }
        }    
    }
    
    public static LocalDateTime parseForDateTime(String datetime) {
        if (datetime == null || datetime.isEmpty()) {
            return null;
        } else {
            Matcher isoDateTime = ISO_LOCAL_DATE_TIME.matcher(datetime);
            if (isoDateTime.find()) {
                return LocalDateTime.parse(isoDateTime.group());
            } else {
                return parse(datetime).atStartOfDay(); 
            }
        }    
    }
}
