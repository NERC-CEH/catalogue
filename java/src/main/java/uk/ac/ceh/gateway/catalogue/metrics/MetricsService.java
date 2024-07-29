package uk.ac.ceh.gateway.catalogue.metrics;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;

@Profile("metrics")
public interface MetricsService {
    void recordView(@NonNull String uuid, @NonNull String addr);
    void recordDownload(@NonNull String uuid, @NonNull String addr);
    int totalViews(@NonNull String uuid);
    int totalDownloads(@NonNull String uuid);
}
