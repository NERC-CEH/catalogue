package uk.ac.ceh.gateway.catalogue.metrics;

import org.springframework.context.annotation.Profile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Profile("metrics")
public interface MetricsService {
    void recordView(@NonNull String uuid, @NonNull String addr);
    void recordDownload(@NonNull String uuid, @NonNull String addr);
    /**
     * Total recorded views of {@code uuid}, or {@code null} if the count is currently unavailable.
     *
     * <p>Nullable rather than a primitive so a metrics outage degrades the counter instead of failing
     * whatever is rendering it: these are read from a record page, where a view count is decorative
     * and must never be able to fail the page. {@code _metrics.ftlh} already treats an absent value as
     * "no counter to show".</p>
     */
    @Nullable Integer totalViews(@NonNull String uuid);

    /** Total recorded downloads of {@code uuid}, or {@code null} if unavailable; see {@link #totalViews}. */
    @Nullable Integer totalDownloads(@NonNull String uuid);
    List<Map<String,String>> getMetricsReport(Instant startDate, Instant endDate, String orderBy, String ordering, List<String> recordType, String docId, Integer noOfRecords);
}
