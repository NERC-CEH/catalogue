package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.common;

import java.time.LocalDate;


public class DateHandler {
    
    public static LocalDate parseEmptyString(String date) {
        if (date.isEmpty()) {
            return null;
        } else {
            return LocalDate.parse(date);
        }
    }
    
}
