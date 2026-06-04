package uk.ac.ceh.gateway.catalogue.wms;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.LegendGraphicMissingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;

import java.util.List;

@Slf4j
@ToString
@RestController
public class OnlineResourceController {
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    private final TMSToWMSGetMapService tmsToWmsGetMapService;
    private final MapServerDetailsService mapServerDetailsService;

    public OnlineResourceController(BundledReaderService<MetadataDocument> documentBundleReader,
                                    GetCapabilitiesObtainerService getCapabilitiesObtainerService,
                                    TMSToWMSGetMapService tmsToWmsGetMapService,
                                    MapServerDetailsService mapServerDetailsService) {
        this.documentBundleReader = documentBundleReader;
        this.getCapabilitiesObtainerService = getCapabilitiesObtainerService;
        this.tmsToWmsGetMapService = tmsToWmsGetMapService;
        this.mapServerDetailsService = mapServerDetailsService;
        log.info("Creating");
    }

    @SneakyThrows
    @GetMapping("documents/{file}/onlineResources")
    public List<OnlineResource> getOnlineResources(@PathVariable String file) {
        log.debug("hit endpoint {}", file);
        return getOnlineResources(documentBundleReader.readBundle(file));
    }

    @SneakyThrows
    @GetMapping("history/{revision}/{file}/onlineResources")
    public List<OnlineResource> getOnlineResources(
            @PathVariable String revision,
            @PathVariable String file
    ) {
        return getOnlineResources(documentBundleReader.readBundle(file, revision));
    }

    private List<OnlineResource> getOnlineResources(MetadataDocument document) {
        log.debug("Looking for {}", document);
        if(document instanceof GeminiDocument geminiDocument) {
            return geminiDocument.getOnlineResources();
        }
        else {
            throw new ResourceNotFoundException("This document is not a gemini document, so does not have online resources");
        }
    }

    @GetMapping("documents/{file}/onlineResources/{index}")
    public Object processOrRedirectToOnlineResource(
            @PathVariable String file,
            @PathVariable int index
    ) {
        return processOrRedirectToOnlineResource(getOnlineResource(file, index));
    }

    @GetMapping("history/{revision}/{file}/onlineResources/{index}")
    public Object processOrRedirectToOnlineResource(
            @PathVariable String revision,
            @PathVariable String file,
            @PathVariable int index
    ) {
        return processOrRedirectToOnlineResource(getOnlineResource(revision, file, index));
    }

    private Object processOrRedirectToOnlineResource(OnlineResource onlineResource) {
        if (onlineResource.getType() == OnlineResource.Type.WMS_GET_CAPABILITIES) {
            return getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
        }
        return new RedirectView(onlineResource.getUrl());
    }

    @GetMapping("documents/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png")
    public TransparentProxy proxyMapProxyTileRequest(
            @PathVariable String file,
            @PathVariable int index,
            @PathVariable String layer,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y
    ) {
        return proxyMapProxyTileRequest(getOnlineResource(file, index), layer, z, x, y);
    }

    @GetMapping("history/{revision}/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png")
    public TransparentProxy proxyMapProxyTileRequest(
            @PathVariable String revision,
            @PathVariable String file,
            @PathVariable int index,
            @PathVariable String layer,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y
    ) {
        return proxyMapProxyTileRequest(getOnlineResource(revision, file, index), layer, z, x, y);
    }

    @SneakyThrows
    private TransparentProxy proxyMapProxyTileRequest(OnlineResource onlineResource, String layer, int z, int x, int y) {
        val wmsCapabilities = getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
        val url = tmsToWmsGetMapService.getWMSMapRequest(wmsCapabilities.getDirectMap(), layer, z, x, y);
        val rewritten = mapServerDetailsService.rewriteToLocalWmsRequest(url);
        return new TransparentProxy(rewritten, MediaType.IMAGE_PNG);
    }

    @GetMapping("documents/{file}/onlineResources/{index}/{layer}/legend")
    public TransparentProxy getMapLayerLegend(
            @PathVariable String file,
            @PathVariable int index,
            @PathVariable String layer
    ) {
        return getMapLayerLegend(getOnlineResource(file, index), layer);
    }

    @GetMapping("history/{revision}/{file}/onlineResources/{index}/{layer}/legend")
    public TransparentProxy getMapLayerLegend(
            @PathVariable String revision,
            @PathVariable String file,
            @PathVariable int index,
            @PathVariable String layer
    ) {
        return getMapLayerLegend(getOnlineResource(revision, file, index), layer);
    }

    @SneakyThrows
    private TransparentProxy getMapLayerLegend(OnlineResource onlineResource, String layer) {
        val wmsCapabilities = getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
        for(val wmsLayer : wmsCapabilities.getLayers()) {
            if(wmsLayer.getName().equals(layer)) {
                if(wmsLayer.getLegendUrl() != null) {
                    val rewritten = mapServerDetailsService.rewriteToLocalWmsRequest(wmsLayer.getLegendUrl());
                    return new TransparentProxy(rewritten, MediaType.parseMediaType("image/*"));
                }
                else {
                    throw new LegendGraphicMissingException("No legend graphic is present for this layer");
                }
            }
        }
        throw new ResourceNotFoundException("The layer: " + layer + " is not present in the given service" );
    }

    @SneakyThrows
    private OnlineResource getOnlineResource(String file, int index) {
        List<OnlineResource> onlineResources = getOnlineResources(file);
        if(index < 0 || onlineResources.size() <= index) {
            throw new ResourceNotFoundException("No online resource exists on this document at index " + index);
        }
        else {
            return onlineResources.get(index);
        }
    }

    @SneakyThrows
    private OnlineResource getOnlineResource(String revision, String file, int index) {
        List<OnlineResource> onlineResources = getOnlineResources(revision, file);
        if(index < 0 || onlineResources.size() <= index) {
            throw new ResourceNotFoundException("No online resource exists on this document at index " + index);
        }
        else {
            return onlineResources.get(index);
        }
    }
}
