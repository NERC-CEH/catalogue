package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.web.client.ResourceAccessException;

/**
 * The preview's Hubbub calls are made on anonymous page views and their failures are not cached, so
 * they must give up rather than hold a request thread while Hubbub hangs.
 */
class DataPreviewConfigTest {

    private static final String RECORD = "rec-1";

    private HttpServer hubbub;
    private final CountDownLatch release = new CountDownLatch(1);

    @BeforeEach
    void startSlowHubbub() throws IOException {
        hubbub = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        hubbub.createContext("/", exchange -> {
            try {
                // Accepts the connection, then never answers until the test is over.
                release.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        hubbub.start();
    }

    @AfterEach
    void stopSlowHubbub() {
        release.countDown();
        hubbub.stop(0);
    }

    private static DataPreviewProperties properties(Duration connectTimeout, Duration readTimeout) {
        return new DataPreviewProperties(
            true, null, null, 10, 50, 262144, 20971520, 200000, Duration.ofSeconds(20), connectTimeout, readTimeout);
    }

    @Test
    void hubbubTimeoutsDefaultToThreeAndTenSeconds() {
        DataPreviewProperties bound = new Binder(new MapConfigurationPropertySource(Map.of()))
            .bindOrCreate("catalogue.data-preview", DataPreviewProperties.class);

        assertThat(bound.hubbubConnectTimeout(), is(Duration.ofSeconds(3)));
        assertThat(bound.hubbubReadTimeout(), is(Duration.ofSeconds(10)));
    }

    @Test
    void listingGivesUpOnAHubbubThatDoesNotAnswer() {
        String address = "http://localhost:" + hubbub.getAddress().getPort();
        PreviewListing listing = new DataPreviewConfig().previewListing(
            properties(Duration.ofSeconds(1), Duration.ofMillis(300)), address, "test-token", "/dropbox");

        long start = System.nanoTime();
        assertThrows(ResourceAccessException.class, () -> listing.manifest(RECORD));
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertThat(elapsed, is(lessThan(3000L)));
    }
}
