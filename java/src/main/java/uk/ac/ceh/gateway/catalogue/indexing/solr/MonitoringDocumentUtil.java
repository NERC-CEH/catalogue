package uk.ac.ceh.gateway.catalogue.indexing.solr;

import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MonitoringDocumentUtil {
    public static List<String> getOperatingPeriod(List<TimePeriod> list) {
        return Optional.ofNullable(list)
            .orElse(Collections.emptyList())
            .stream()
            .filter((timePeriod -> (timePeriod.getBegin() != null || timePeriod.getEnd() != null)))
            .map(timePeriod -> {
                String begin = timePeriod.getBegin() != null? timePeriod.getBegin().toString(): "*";
                String end = timePeriod.getEnd() != null? timePeriod.getEnd().toString(): "*";
                return String.format("[%s TO %s]", begin, end);
            })
            .collect(Collectors.toList());
    }
}
