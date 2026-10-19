package uk.ac.ceh.gateway.catalogue.wms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.document.CatalogueRecordIds;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.Service.CoupledResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds the WMS maps a record page can preview. Used from FreeMarker as
 * {@code wmsMaps}, so it only reads cache-backed local records and never
 * returns a service the current user cannot view.
 */
@Slf4j
@Service
public class WmsMapService {
    private static final String LAYER_PLACEHOLDER = "-";

    private final BundledReaderService<MetadataDocument> documentReader;
    private final PermissionService permissionService;
    private final MapServerDetailsService mapServerDetailsService;
    private final String baseUri;

    public WmsMapService(
        BundledReaderService<MetadataDocument> documentReader,
        PermissionService permissionService,
        MapServerDetailsService mapServerDetailsService,
        @Value("${documents.baseUri}") String baseUri
    ) {
        this.documentReader = documentReader;
        this.permissionService = permissionService;
        this.mapServerDetailsService = mapServerDetailsService;
        this.baseUri = baseUri;
    }

    /**
     * FreeMarker cannot hand a template its own data-model root to a Java method (it is
     * a merged hash view, not the bean), so templates look the record up by id. The read is
     * cache-backed, and anything unreadable or not a Gemini record has no maps.
     */
    public List<WmsMap> forRecordId(String recordId) {
        if (recordId == null || recordId.isBlank()) {
            return Collections.emptyList();
        }
        try {
            if (documentReader.readBundle(recordId) instanceof GeminiDocument document) {
                return forRecord(document);
            }
        } catch (Exception e) {
            log.warn("Skipping WMS preview of record {}", recordId, e);
        }
        return Collections.emptyList();
    }

    public List<WmsMap> forRecord(GeminiDocument document) {
        if (document == null) {
            return Collections.emptyList();
        }
        if ("service".equalsIgnoreCase(document.getType())) {
            return mapsOf(document, Collections.emptyList());
        }
        List<Link> services = document.getRelIsRequiredBy();
        if (services == null || services.isEmpty()) {
            return Collections.emptyList();
        }
        CatalogueUser user = currentUser();
        List<WmsMap> maps = new ArrayList<>();
        for (Link link : services) {
            maps.addAll(mapsForLinkedService(link, document.getId(), user));
        }
        return maps;
    }

    private List<WmsMap> mapsForLinkedService(Link link, String datasetId, CatalogueUser user) {
        String serviceId = lastSegment(link.getHref());
        if (serviceId.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            if (!permissionService.toAccess(user, serviceId, "VIEW")) {
                return Collections.emptyList();
            }
            if (documentReader.readBundle(serviceId) instanceof GeminiDocument service) {
                return mapsOf(service, layerNamesFor(service, datasetId));
            }
        } catch (Exception e) {
            log.warn("Skipping WMS preview of service {}", serviceId, e);
        }
        return Collections.emptyList();
    }

    private List<String> layerNamesFor(GeminiDocument service, String datasetId) {
        if (service.getService() == null) {
            return Collections.emptyList();
        }
        return service.getService().getCoupledResources().stream()
            .filter(cr -> CatalogueRecordIds.recordId(cr.getIdentifier(), baseUri)
                .filter(id -> id.equals(datasetId))
                .isPresent())
            .map(CoupledResource::getLayerName)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(name -> !name.isEmpty() && !LAYER_PLACEHOLDER.equals(name))
            .distinct()
            .toList();
    }

    private List<WmsMap> mapsOf(GeminiDocument service, List<String> layerNames) {
        if (service.getOnlineResources() == null) {
            return Collections.emptyList();
        }
        List<WmsMap> maps = new ArrayList<>();
        for (OnlineResource resource : service.getOnlineResources()) {
            Optional<String> mapId = mapServerDetailsService.getLocalMapId(resource);
            mapId.ifPresent(id -> maps.add(new WmsMap(
                service.getId(),
                service.getTitle(),
                resource.getUrl(),
                withoutQuery(resource.getUrl()),
                "/maps/" + id,
                layerNames
            )));
        }
        return maps;
    }

    private static String withoutQuery(String url) {
        int query = url.indexOf('?');
        return query < 0 ? url : url.substring(0, query);
    }

    private static String lastSegment(String href) {
        String path = withoutQuery(href == null ? "" : href);
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static CatalogueUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
            ? (CatalogueUser) authentication.getPrincipal()
            : CatalogueUser.PUBLIC_USER;
    }
}
