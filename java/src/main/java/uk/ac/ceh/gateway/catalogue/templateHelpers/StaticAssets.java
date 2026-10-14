package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * The URLs of the scripts and stylesheets webpack builds, shared with templates as {@code assets}:
 * <pre>{@code <#list assets.files("main") as src><script defer src="${src}"></script></#list>}</pre>
 *
 * <p>Each file name carries a hash of its content, so the files can be cached for a year
 * ({@code StaticAssetsCacheConfig}) and a deploy is still seen at once: a changed file is a new URL.
 * webpack lists them in {@code manifest.json}, beside them, per entry and in the order a page loads
 * them (NERC-CEH/dri-one#364). The manifest is read again whenever it changes, as it does on every
 * rebuild under {@code npm run watch}.
 */
@Slf4j
public class StaticAssets {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<Map<String, List<String>>> MANIFEST = new TypeReference<>() {};
    private static final long MISSING = -1;

    private final Resource manifest;
    private volatile long loaded = Long.MIN_VALUE;
    private volatile Map<String, List<String>> files = Map.of();

    public StaticAssets(Resource manifest) {
        this.manifest = manifest;
    }

    /** The assets in a static location, as spring.web.resources.static-locations gives it */
    public static StaticAssets in(String staticLocation) {
        String directory = staticLocation.endsWith("/") ? staticLocation : staticLocation + "/";
        return new StaticAssets(new DefaultResourceLoader().getResource(directory + "manifest.json"));
    }

    /** The URLs of an entry's files, in the order to load them, or none if webpack didn't build it. */
    public List<String> files(String entry) {
        refresh();
        return files.getOrDefault(entry, List.of());
    }

    private void refresh() {
        long modified = lastModified();
        if (modified == loaded) {
            return;
        }
        synchronized (this) {
            if (modified == loaded) {
                return;
            }
            if (modified == MISSING) {
                log.warn("No {}: pages link no scripts or stylesheets until webpack has built them", manifest);
                files = Map.of();
            } else {
                try (InputStream in = manifest.getInputStream()) {
                    files = JSON.readValue(in, MANIFEST);
                } catch (IOException | JacksonException e) {
                    // Most likely caught mid-write by the watcher: keep the last good one and try again
                    log.warn("Could not read {}, will try again: {}", manifest, e.getMessage());
                    return;
                }
            }
            loaded = modified;
        }
    }

    private long lastModified() {
        try {
            return manifest.lastModified();
        } catch (IOException e) {
            return MISSING;
        }
    }
}
