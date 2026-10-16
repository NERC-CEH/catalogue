package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

@DisplayName("StaticAssets")
class StaticAssetsTest {
    @TempDir Path static_;

    private void manifest(String json, Instant modified) throws IOException {
        Path manifest = static_.resolve("manifest.json");
        Files.writeString(manifest, json);
        Files.setLastModifiedTime(manifest, FileTime.from(modified));
    }

    private StaticAssets assets() {
        // As spring.web.resources.static-locations gives it, without the trailing slash compose uses
        return StaticAssets.in("file:" + static_);
    }

    @Test
    @DisplayName("lists an entry's files from the build's manifest, in order")
    void listsAnEntrysFiles() throws IOException {
        manifest("""
            {"main": ["/static/scripts/vendor.1a2b3c4d.js", "/static/scripts/main.5e6f7a8b.js"],
             "base": ["/static/css/base.9c0d1e2f.css"]}""", Instant.parse("2026-10-14T09:00:00Z"));

        StaticAssets assets = assets();

        assertThat(assets.files("main"), contains("/static/scripts/vendor.1a2b3c4d.js", "/static/scripts/main.5e6f7a8b.js"));
        assertThat(assets.files("base"), contains("/static/css/base.9c0d1e2f.css"));
    }

    @Test
    @DisplayName("lists nothing for an entry the build didn't write")
    void nothingForAnUnknownEntry() throws IOException {
        manifest("{\"main\": [\"/static/scripts/main.5e6f7a8b.js\"]}", Instant.parse("2026-10-14T09:00:00Z"));

        assertThat(assets().files("style-nowhere"), empty());
    }

    @Test
    @DisplayName("lists nothing, rather than failing the page, before the build has run")
    void nothingWithoutAManifest() {
        assertThat(assets().files("main"), empty());
    }

    @Test
    @DisplayName("reads the manifest again when the build reruns")
    void readsARebuild() throws IOException {
        StaticAssets assets = assets();
        manifest("{\"main\": [\"/static/scripts/main.11111111.js\"]}", Instant.parse("2026-10-14T09:00:00Z"));
        assertThat(assets.files("main"), contains("/static/scripts/main.11111111.js"));

        manifest("{\"main\": [\"/static/scripts/main.22222222.js\"]}", Instant.parse("2026-10-14T09:00:05Z"));

        assertThat(assets.files("main"), contains("/static/scripts/main.22222222.js"));
    }

    @Test
    @DisplayName("keeps the last good manifest when it catches one half-written, and reads it once complete")
    void survivesAHalfWrittenManifest() throws IOException {
        StaticAssets assets = assets();
        manifest("{\"main\": [\"/static/scripts/main.11111111.js\"]}", Instant.parse("2026-10-14T09:00:00Z"));
        assertThat(assets.files("main"), contains("/static/scripts/main.11111111.js"));

        manifest("{\"main\": [\"/static/scri", Instant.parse("2026-10-14T09:00:05Z"));
        assertThat(assets.files("main"), contains("/static/scripts/main.11111111.js"));

        manifest("{\"main\": [\"/static/scripts/main.22222222.js\"]}", Instant.parse("2026-10-14T09:00:05Z"));
        assertThat(assets.files("main"), contains("/static/scripts/main.22222222.js"));
    }
}
