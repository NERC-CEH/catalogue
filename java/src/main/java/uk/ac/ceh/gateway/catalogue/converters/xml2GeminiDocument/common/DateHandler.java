package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.common;

import org.joda.time.LocalDate;


public class DateHandler {
    
    public static LocalDate parseEmptyString(String date) {
        if (date.isEmpty()) {
            return null;
        } else {
            return LocalDate.parse(date);
        }
    }
    
}
