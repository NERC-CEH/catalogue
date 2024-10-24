package uk.ac.ceh.gateway.catalogue.metrics;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Profile("metrics")
public interface MetricsService {
    void recordView(@NonNull String uuid, @NonNull String addr);
    void recordDownload(@NonNull String uuid, @NonNull String addr);
    int totalViews(@NonNull String uuid);
    int totalDownloads(@NonNull String uuid);
    List<Map<String,String>> getMetricsReport(Instant startDate, Instant endDate, String orderBy, String ordering, List<String> recordType, String docId, Integer noOfRecords);
}
