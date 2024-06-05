package uk.ac.ceh.gateway.catalogue.services;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;

@Profile("metrics")
public interface MetricsService {
    void recordView(@NonNull String uuid, @NonNull String addr);
    void recordDownload(@NonNull String uuid, @NonNull String addr);
}
