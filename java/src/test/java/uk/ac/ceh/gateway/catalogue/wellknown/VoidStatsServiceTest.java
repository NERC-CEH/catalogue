package uk.ac.ceh.gateway.catalogue.wellknown;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VoidStatsService")
class VoidStatsServiceTest {

    private final VoidStatsService service = new VoidStatsService();

    @Test
    @DisplayName("get returns empty when no stats have been set")
    void getReturnsEmptyWhenNoStats() {
        assertThat(service.get("eidc")).isEmpty();
    }

    @Test
    @DisplayName("get returns stats after update")
    void getReturnsStatsAfterUpdate() {
        service.update("eidc", new VoidStats(42L, 1000L, Map.of()));

        assertThat(service.get("eidc"))
            .isPresent()
            .hasValueSatisfying(s -> assertThat(s.entities()).isEqualTo(42L));
    }

    @Test
    @DisplayName("update for one catalogue does not affect another")
    void updateIsPerCatalogue() {
        service.update("eidc", new VoidStats(10L, 500L, Map.of()));

        assertThat(service.get("ukeof")).isEmpty();
    }
}
