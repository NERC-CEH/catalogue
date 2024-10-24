package uk.ac.ceh.gateway.catalogue.metrics;

import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Profile("metrics")
@RestController
@RequestMapping("/{catalogue}/metrics")
public class MetricsReportController {

    private final MetricsService metricsService;

    public MetricsReportController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping(produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public MetricsReportModel getMetricsReport(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) String orderBy,
        @RequestParam(required = false) String ordering,
        @RequestParam(required = false) List<String> recordType,
        @RequestParam(required = false) String docId,
        @RequestParam(required = false) Integer noOfRecords,
        @PathVariable String catalogue
    ) {

        Instant startInstant = parseDate(startDate);
        Instant endInstant = parseDate(endDate);

        if (startInstant != null && endInstant != null && startInstant.isAfter(endInstant)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        return new MetricsReportModel(catalogue, metricsService.getMetricsReport(startInstant, endInstant, orderBy, ordering, recordType, docId, noOfRecords));
    }

    private Instant parseDate(String date) {
        return Optional.ofNullable(date)
            .filter(d -> !d.isBlank())
            .map(d -> LocalDate.parse(d).atStartOfDay().toInstant(ZoneOffset.UTC))
            .orElse(null);
    }
}
