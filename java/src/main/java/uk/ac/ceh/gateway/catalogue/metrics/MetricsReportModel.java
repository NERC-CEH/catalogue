package uk.ac.ceh.gateway.catalogue.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.List;
import java.util.Map;

@ConvertUsing({
    @Template(called="html/metrics/metrics.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
@Data
public class MetricsReportModel{

    public String catalogue;
    /**
     * Always sent, even when empty. The app's mapper leaves out empty lists (non_empty), so a report
     * that found nothing had no metricsReport at all, and the page kept showing the previous rows.
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public List<Map<String, String>> metricsReport;

    public MetricsReportModel(String catalogue, List<Map<String, String>> metricsReport) {
        this.catalogue = catalogue;
        this.metricsReport = metricsReport;
    }

    /** The page's default "No. of records", from the service so the two cannot drift. Not sent as JSON. */
    @JsonIgnore
    public int getDefaultReportLimit() {
        return MetricsService.DEFAULT_REPORT_LIMIT;
    }

    /** The most rows the report will return, for the page's "No. of records" maximum. Not sent as JSON. */
    @JsonIgnore
    public int getMaxReportLimit() {
        return MetricsService.MAX_REPORT_LIMIT;
    }
}
