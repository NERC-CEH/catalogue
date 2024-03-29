package uk.ac.ceh.gateway.catalogue.wms;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute.Bucket;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.Projection;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.ac.ceh.gateway.catalogue.controllers.MapViewerController.MAPSERVER;

/**
 * The following service is a 'helper' which produces text which is useful in
 * the creation of map server mapfiles.
 */
@Slf4j
@ToString
@Service
public class MapServerDetailsService {
    private final String hostUrl;

    public MapServerDetailsService(
        @Value("${documents.baseUri}") String hostUrl
    ) {
        this.hostUrl = hostUrl;
        log.info("Creating {}", this);
    }

    /**
     * For the given document, return the potential wms endpoint where the
     * service could be hosted.
     * @param id of the document to generate a wms endpoint for
     * @return wms url
     */
    public String getWmsUrl(String id) {
        return String.format(
            "%s/maps/%s?",
            hostUrl,
            id
        );
    }

    /**
     * Locate a MapDataDefinition object from the supplied MetadataDocument (if
     * it has one) else return null
     * @param document to get a map data definition from
     * @return the map data definition for the given metadata document (or null)
     */
    public MapDataDefinition getMapDataDefinition(MetadataDocument document) {
        if (document instanceof GeminiDocument geminiDocument) {
            val possibleMapDataDefinition = Optional.ofNullable(geminiDocument.getMapDataDefinition());
            if (possibleMapDataDefinition.isPresent()) {
                val mapDataDefinition = possibleMapDataDefinition.get();
                val possibleData = Optional.ofNullable(mapDataDefinition.getData());
                if (possibleData.isPresent()) {
                    if (!mapDataDefinition.getData().isEmpty()) {
                        return mapDataDefinition;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Scan over the datasources defined in the supplied map data definition and
     * return a list of all the projection systems which are used.
     * @param mapDataDefinition to scan
     * @return a list of projection systems
     */
    public List<String> getProjectionSystems(MapDataDefinition mapDataDefinition) {
        return mapDataDefinition
                .getData()
                .stream()
                .flatMap((m) -> Stream.concat(
                        Stream.of(m.getEpsgCode()),
                        Optional.ofNullable(m.getReprojections())
                                .orElseGet(Collections::emptyList)
                                .stream()
                                .map(Projection::getEpsgCode)
                )).distinct().collect(Collectors.toList());
    }

    /**
     * Scan the supplied datasource and locate projection details (path and
     * espgCode) which match the supplied desired epsgCode or fallback to the
     * default details (i.e. the supplied primary datasource)
     * @param primary
     * @param desiredEpsgCode
     * @return either a matching path and epsg code or the supplied datasource
     */
    public Projection getFavouredProjection(DataSource primary, String desiredEpsgCode) {
        if(primary.getEpsgCode().equalsIgnoreCase(desiredEpsgCode)) {
            return primary;
        }
        return Optional.ofNullable(primary.getReprojections())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter((p) -> p.getEpsgCode().equalsIgnoreCase(desiredEpsgCode))
                .findFirst()
                .orElse(primary);
    }

    /**
     * Takes the supplied wms url and determines if the request contacts an
     * endpoint of this application. If it does, then the request is rewritten
     * to contact the hosted mapserver instance directly. This avoids a double
     * proxying of the request by contacting the hosted mapserver instance
     * directly.
     * @param wmsUrl
     * @return
     */
    public String rewriteToLocalWmsRequest(String wmsUrl) {
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(wmsUrl).build();
        List<String> pathSegments = uri.getPathSegments();
        if(wmsUrl.startsWith(hostUrl + "/maps") && pathSegments.size() == 2) {
            return getLocalWMSRequest(pathSegments.get(1), uri.getQuery());
        }
        return wmsUrl;
    }

    /**
     * For the given list of buckets extract the range of values used and the
     * fewest amount of equal sized buckets required to classify a raster in
     * the specified buckets.
     * @param buckets to evaluate
     * @return min, max and smallest amount of equal sized buckets to create
     */
    public MapBucketDetails getScaledBuckets(List<Bucket> buckets) {
        if(buckets.stream().allMatch((b) -> nonNull(b.getMin()) && nonNull(b.getMax()))) {
            BigDecimal min = buckets.stream().map(Bucket::getMin).min(BigDecimal::compareTo).get();
            BigDecimal max = buckets.stream().map(Bucket::getMax).max(BigDecimal::compareTo).get();
            BigDecimal bucketSize = buckets.stream().map((b) -> b.getMax().subtract(b.getMin())).min(BigDecimal::compareTo).get();
            BigDecimal bucketCount = max.subtract(min).divide(bucketSize, RoundingMode.UP).setScale(0, RoundingMode.UP);

            if(bucketCount.compareTo(BigDecimal.ZERO) > 0) {
                return new MapBucketDetails(min, max, bucketCount.intValueExact());
            }
        }
        return null;
    }

    /**
     * Generate a wms url which contacts the catalogues mapserver instance for
     * the given id and query string.
     * @param id of the wms service to call
     * @param query string containing wms request parameters
     * @return a wms url to request
     */
    public String getLocalWMSRequest(String id, String query) {
        return UriComponentsBuilder
                .fromHttpUrl(MAPSERVER)
                .query(query)
                .buildAndExpand(id)
                .toUriString();
    }

    @lombok.Value
    public static class MapBucketDetails {
        BigDecimal min, max;
        int buckets;
    }
}
