package uk.ac.ceh.gateway.catalogue.indexing.solr;

import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;

public class MonitoringDocumentUtil {
    public static String getTimeRange(TimePeriod timePeriod) {
        String begin = timePeriod.getBegin() != null ? timePeriod.getBegin().toString() : "*";
        String end = timePeriod.getEnd() != null ? timePeriod.getEnd().toString() : "*";
        return String.format("[%s TO %s]", begin, end);
    }
}
