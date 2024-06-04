package uk.ac.ceh.gateway.catalogue.services;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;

import java.net.InetAddress;

@Profile("metrics")
public interface MetricsService {
    void recordView(@NonNull String uuid, @NonNull InetAddress addr);
    void recordDownload(@NonNull String uuid, @NonNull InetAddress addr);
}
