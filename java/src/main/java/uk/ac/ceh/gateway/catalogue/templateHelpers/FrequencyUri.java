package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Maps an ISO 19115 maintenance frequency code to the EU Publications Office
 * frequency IRI.
 *
 * <p>Every ISO code listed here has a live, non-deprecated target in the EU
 * frequency authority table. Blank, null and unrecognised values yield
 * {@code ""} so the triple is omitted rather than coerced.
 */
@Component
public class FrequencyUri {

    private static final String BASE = "http://publications.europa.eu/resource/authority/frequency/";

    private static final Map<String, String> CODES = Map.ofEntries(
        Map.entry("continual", "CONT"),
        Map.entry("daily", "DAILY"),
        Map.entry("weekly", "WEEKLY"),
        Map.entry("fortnightly", "BIWEEKLY"),
        Map.entry("monthly", "MONTHLY"),
        Map.entry("quarterly", "QUARTERLY"),
        Map.entry("biannually", "ANNUAL_2"),
        Map.entry("annually", "ANNUAL"),
        Map.entry("irregular", "IRREG"),
        Map.entry("unknown", "UNKNOWN"),
        Map.entry("asNeeded", "AS_NEEDED"),
        Map.entry("notPlanned", "NOT_PLANNED")
    );

    public String uri(String isoFrequencyCode) {
        if (isoFrequencyCode == null) {
            return "";
        }
        String target = CODES.get(isoFrequencyCode.trim());
        return target == null ? "" : BASE + target;
    }
}
