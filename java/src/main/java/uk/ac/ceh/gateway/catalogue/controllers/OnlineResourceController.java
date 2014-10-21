package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.LegendGraphicMissingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.NoSuchOnlineResourceException;
import uk.ac.ceh.gateway.catalogue.mvc.TransparentProxyView;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Controller
@Slf4j
public class OnlineResourceController {
    private final DataRepository<CatalogueUser> repo;
    private final CloseableHttpClient httpClient;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    private final TMSToWMSGetMapService tmsToWmsGetMapService;
    
    @Autowired
    public OnlineResourceController(DataRepository<CatalogueUser> repo,
                                    CloseableHttpClient httpClient,
                                    BundledReaderService<MetadataDocument> documentBundleReader,
                                    GetCapabilitiesObtainerService getCapabilitiesObtainerService,
                                    TMSToWMSGetMapService tmsToWmsGetMapService) {
        this.repo = repo;
        this.httpClient = httpClient;
        this.documentBundleReader = documentBundleReader;
        this.getCapabilitiesObtainerService = getCapabilitiesObtainerService;
        this.tmsToWmsGetMapService = tmsToWmsGetMapService;
    }
    
    @RequestMapping (value = "documents/{file}/onlineResources/{index}",
                     method = RequestMethod.GET)
    @ResponseBody
    public Object processOrRedirectToOnlineResource(
            @PathVariable("file") String file,
            @PathVariable("index") int index) throws DataRepositoryException, IOException, UnknownContentTypeException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        return processOrRedirectToOnlineResource(latestRev.getRevisionID(), file, index);
    }
    
    @RequestMapping (value = "history/{revision}/{file}/onlineResources/{index}",
                     method = RequestMethod.GET)
    @ResponseBody
    public Object processOrRedirectToOnlineResource(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index) throws DataRepositoryException, IOException, UnknownContentTypeException {
        
        OnlineResource onlineResource = getOnlineResource(documentBundleReader.readBundle(file, revision), index);
        switch(onlineResource.getType()) {
            case WMS_GET_CAPABILITIES : 
                return getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
            default: 
                return new RedirectView(onlineResource.getUrl());
        }
    }
    
    @RequestMapping (value = "documents/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png",
                     method = RequestMethod.GET)
    public TransparentProxyView proxyMapProxyTileRequest(
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer,
            @PathVariable("z") int z,
            @PathVariable("x") int x,
            @PathVariable("y") int y) throws IOException, UnknownContentTypeException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        return proxyMapProxyTileRequest(latestRev.getRevisionID(), file, index, layer, z, x, y);
    }
    
    @RequestMapping (value = "history/{revision}/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png",
                     method = RequestMethod.GET)
    public TransparentProxyView proxyMapProxyTileRequest(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer,
            @PathVariable("z") int z,
            @PathVariable("x") int x,
            @PathVariable("y") int y) throws IOException, UnknownContentTypeException {
        OnlineResource resource = getOnlineResource(file, revision, index); 
        WmsCapabilities wmsCapabilities = getCapabilitiesObtainerService.getWmsCapabilities(resource);
        String url = tmsToWmsGetMapService.getWMSMapRequest(wmsCapabilities.getDirectMap(), layer, z, x, y);
        
        return new TransparentProxyView(httpClient, url);
    }
    
    @RequestMapping (value = "documents/{file}/onlineResources/{index}/{layer}/legend",
                     method = RequestMethod.GET)
    public TransparentProxyView getMapLayerLegend(
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer) throws IOException, UnknownContentTypeException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        return getMapLayerLegend(latestRev.getRevisionID(), file, index, layer);
    }
    
    @RequestMapping (value = "history/{revision}/{file}/onlineResources/{index}/{layer}/legend",
                     method = RequestMethod.GET)
    public TransparentProxyView getMapLayerLegend(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer) throws IOException, UnknownContentTypeException {
        OnlineResource onlineResource = getOnlineResource(documentBundleReader.readBundle(file, revision), index);
        WmsCapabilities wmsCapabilities = getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
        for(Layer wmsLayer : wmsCapabilities.getLayers()) {
            if(wmsLayer.getName().equals(layer)) {
                if(wmsLayer.getLegendUrl() != null) {
                    return new TransparentProxyView(httpClient, wmsLayer.getLegendUrl());
                }
                else {
                    throw new LegendGraphicMissingException("No legend graphic is present for this layer");
                }
            }
        }
        throw new IllegalArgumentException("The layer: " + layer + " is not present in the given service" );
    }
    
    protected OnlineResource getOnlineResource(String file, String revision, int index) throws IOException, UnknownContentTypeException {
        return getOnlineResource(documentBundleReader.readBundle(file, revision), index);
    }
    
    protected OnlineResource getOnlineResource(MetadataDocument document, int index) {
        if(document instanceof GeminiDocument) {
            GeminiDocument geminiDocument = (GeminiDocument)document;
            List<OnlineResource> onlineResources = geminiDocument.getOnlineResources();
            if(index < 0 || onlineResources.size() <= index) {
                throw new NoSuchOnlineResourceException("No online resource exists on this document at index " + index);
            }
            else {
                return onlineResources.get(index);
            }
        }
        else {
            throw new NoSuchOnlineResourceException("This document is not a gemini document, so does not have online resources");
        }
    }
}
