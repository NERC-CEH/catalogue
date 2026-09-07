package uk.ac.ceh.gateway.catalogue.ogc;

import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.model.MapServerException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.MAPSERVER_GML_VALUE;

/**
 * The following simple controller just hands off a request to on of our
 * templates
 */
@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@CrossOrigin
@Controller
@RequestMapping(value="maps")
public class MapViewerController {
    public static final String INFO_FORMAT = "INFO_FORMAT";
    private final static Set<String> LOCAL_INFO_FORMATS = ImmutableSet.of("text/xml", "application/json");
    private final static Set<String> IMAGE_DIMENSIONS = ImmutableSet.of("WIDTH", "HEIGHT");

    private final RestTemplate rest;
    private final String mapserverUrl;
    private final boolean camptocampMode;
    private final int maxImageSize;

    public MapViewerController(
            @Qualifier("wms") RestTemplate rest,
            @Value("${mapserver.url:http://mapserver/{id}}") String mapserverUrl,
            @Value("${mapserver.camptocamp:false}") boolean camptocampMode,
            @Value("${mapserver.max-image-size:2048}") int maxImageSize
    ) {
        this.rest = rest;
        this.mapserverUrl = mapserverUrl;
        this.camptocampMode = camptocampMode;
        this.maxImageSize = maxImageSize;
        log.info("Creating");
    }

    @SuppressWarnings("SpringMVCViewInspection")
    @GetMapping
    public String loadMapViewer() {
        return "/html/mapviewer";
    }

    @RequestMapping(value = "{file}")
    @ResponseBody
    @SneakyThrows
    public Object wmsService(
            @PathVariable String file,
            @RequestParam MultiValueMap<String, String> params
    ) {
        if (log.isDebugEnabled()) {
            params.forEach((String name, List<String> values) -> log.debug("{} + {}", name, values));
        }

        checkRequestedImageSize(params);

        if (isLocalGetFeatureInfoRequest(params)) {
            params.set(INFO_FORMAT, MAPSERVER_GML_VALUE);
            URI url = getLocalWMSRequest(file, params);
            log.debug("local WMS request: {}", url);
            return rest.getForObject(url, WmsFeatureInfo.class);
        }
        return new TransparentProxy(getLocalWMSRequest(file, params), null);
    }

    /**
     * MapServer allocates a buffer proportional to the requested image dimensions, so an
     * unbounded WIDTH or HEIGHT lets a single request exhaust the container's memory. Reject
     * anything above the limit here rather than forwarding it, since parameters reach us
     * verbatim from the caller and WMS clients are free to use any case for parameter names.
     */
    private void checkRequestedImageSize(MultiValueMap<String, String> params) {
        params.forEach((String name, List<String> values) -> {
            if (IMAGE_DIMENSIONS.contains(name.toUpperCase(Locale.ROOT))) {
                values.forEach(value -> checkDimension(name, value));
            }
        });
    }

    private void checkDimension(String name, String value) {
        final int dimension;
        try {
            dimension = Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            // Not our problem to validate; MapServer rejects it cheaply without allocating
            log.debug("Ignoring non-numeric {}: {}", name, value);
            return;
        }
        if (dimension > maxImageSize) {
            log.warn("Rejecting WMS request with {} of {}, maximum is {}", name, dimension, maxImageSize);
            throw new MapServerException(
                "Requested %s of %d exceeds the maximum image size of %d".formatted(name, dimension, maxImageSize),
                HttpStatus.BAD_REQUEST,
                MediaType.TEXT_PLAIN
            );
        }
    }

    private boolean isLocalGetFeatureInfoRequest(MultiValueMap<String, String> params) {
        val isWms = Optional.ofNullable(params.getFirst("SERVICE"))
            .orElse("")
            .equalsIgnoreCase("wms");
        val isFeatureInfo = Optional.ofNullable(params.getFirst("REQUEST"))
            .orElse("")
            .equalsIgnoreCase("getFeatureInfo");
        val isInfoFormat = Optional.ofNullable(params.get(INFO_FORMAT))
            .orElse(Collections.emptyList()).stream()
            .anyMatch(LOCAL_INFO_FORMATS::contains);
        log.debug("is WMS? {}, featureInfo? {}, infoFormat? {}", isWms, isFeatureInfo, isInfoFormat);
        return isWms && isFeatureInfo && isInfoFormat;
    }

    private URI getLocalWMSRequest(String id, MultiValueMap<String, String> params) {
        if (camptocampMode) {
            if (params.keySet().stream().noneMatch(key -> key.equalsIgnoreCase("map"))) {
                params.set("map", "/maps/" + id + "_default.map");
            }
            if (params.keySet().stream().noneMatch(key -> key.equalsIgnoreCase("STYLES"))) {
                params.set("STYLES", "");
            }
        }
        URI uri = UriComponentsBuilder
            .fromUriString(mapserverUrl)
            .queryParams(params)
            .buildAndExpand(id)
            .toUri();

        log.info("WMS request URL built: {}", uri);
        return uri;
    }
}
