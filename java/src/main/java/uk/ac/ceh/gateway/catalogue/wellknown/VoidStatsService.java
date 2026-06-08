package uk.ac.ceh.gateway.catalogue.wellknown;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Profile("exports")
@Slf4j
@ToString
@Service
public class VoidStatsService {

    private final ConcurrentHashMap<String, VoidStats> stats = new ConcurrentHashMap<>();

    public void update(String catalogueId, VoidStats voidStats) {
        stats.put(catalogueId, voidStats);
        log.info("Updated VoID stats for {}: entities={}", catalogueId, voidStats.entities());
    }

    public Optional<VoidStats> get(String catalogueId) {
        return Optional.ofNullable(stats.get(catalogueId));
    }

    public void remove(String catalogueId) {
        stats.remove(catalogueId);
        log.info("Cleared VoID stats for {}", catalogueId);
    }
}
