package uk.ac.ceh.gateway.catalogue.metrics;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Views and downloads report across all documents.
 *
 * <p>Unlike the per-document counters, this aggregates across the whole metrics table, so it is
 * restricted to system admins (same gate as {@code MaintenanceController}/{@code SparqlController}),
 * its row count is capped at {@link MetricsService#MAX_REPORT_LIMIT}, and {@code docId} is an exact
 * document ID so it can use the primary-key index.</p>
 */
@Profile("metrics")
@RestController
@RequestMapping("/{catalogue}/metrics")
@Secured(DocumentController.MAINTENANCE_ROLE)
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

        LocalDate from = parseDate(startDate);
        LocalDate to = parseDate(endDate);

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Instant startInstant = from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC);
        // The To date includes that day: the report keeps rows that end by this bound, so it is the
        // start of the next day, not of the To day itself (dri-one #461)
        Instant endInstant = to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        String documentId = null;
        if (docId != null && !docId.isBlank()) {
            Optional<String> parsed = parseDocId(docId);
            if (parsed.isEmpty()) {
                return new MetricsReportModel(catalogue, List.of());
            }
            documentId = parsed.get();
        }

        return new MetricsReportModel(catalogue, metricsService.getMetricsReport(
            startInstant, endInstant, orderBy, ordering, recordType,
            documentId, MetricsService.reportLimit(noOfRecords)));
    }

    private LocalDate parseDate(String date) {
        return Optional.ofNullable(date)
            .filter(d -> !d.isBlank())
            .map(LocalDate::parse)
            .orElse(null);
    }

    /** The document ID in canonical (lower-case) form, or empty if it is not a whole UUID. */
    private Optional<String> parseDocId(String docId) {
        try {
            return Optional.of(UUID.fromString(docId.strip()).toString());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
