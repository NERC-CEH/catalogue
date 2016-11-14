package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.LegendGraphicMissingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxyException;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Controller
public class OnlineResourceController {
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    private final TMSToWMSGetMapService tmsToWmsGetMapService;
    private final MapServerDetailsService mapServerDetailsService;
    
    @Autowired
    public OnlineResourceController(BundledReaderService<MetadataDocument> documentBundleReader,
                                    GetCapabilitiesObtainerService getCapabilitiesObtainerService,
                                    TMSToWMSGetMapService tmsToWmsGetMapService,
                                    MapServerDetailsService mapServerDetailsService) {
        this.documentBundleReader = documentBundleReader;
        this.getCapabilitiesObtainerService = getCapabilitiesObtainerService;
        this.tmsToWmsGetMapService = tmsToWmsGetMapService;
        this.mapServerDetailsService = mapServerDetailsService;
    }
    
    @RequestMapping (value = "documents/{file}/onlineResources",
                     method = RequestMethod.GET)
    @ResponseBody
    public List<OnlineResource> getOnlineResources(
            @PathVariable("file") String file
    ) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        return getOnlineResources(documentBundleReader.readBundle(file));
    }
    
    @RequestMapping (value = "history/{revision}/{file}/onlineResources",
                     method = RequestMethod.GET)
    @ResponseBody
    public List<OnlineResource> getOnlineResources(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file
    ) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        return getOnlineResources(documentBundleReader.readBundle(file, revision));
    }
    
    private List<OnlineResource> getOnlineResources(MetadataDocument document) {
        if(document instanceof GeminiDocument) {
            GeminiDocument geminiDocument = (GeminiDocument)document;
            return geminiDocument.getOnlineResources();
        }
        else {
            throw new ResourceNotFoundException("This document is not a gemini document, so does not have online resources");
        }
    }

    
    @RequestMapping (value = "documents/{file}/onlineResources/{index}",
                     method = RequestMethod.GET)
    @ResponseBody
    public Object processOrRedirectToOnlineResource(
            @PathVariable("file") String file,
            @PathVariable("index") int index) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException, URISyntaxException {
        return processOrRedirectToOnlineResource(getOnlineResource(file, index));
    }
    
    @RequestMapping (value = "history/{revision}/{file}/onlineResources/{index}",
                     method = RequestMethod.GET)
    @ResponseBody
    public Object processOrRedirectToOnlineResource(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException, URISyntaxException {
        
        return processOrRedirectToOnlineResource(getOnlineResource(revision, file, index));
    }
    
    private Object processOrRedirectToOnlineResource(OnlineResource onlineResource) throws URISyntaxException {
        switch(onlineResource.getType()) {
            case WMS_GET_CAPABILITIES : 
                return getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
            default: 
                return new RedirectView(onlineResource.getUrl());
        }
    }
    
    @RequestMapping (value    = "documents/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png",
                     method   = RequestMethod.GET)
    @ResponseBody
    public TransparentProxy proxyMapProxyTileRequest(
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer,
            @PathVariable("z") int z,
            @PathVariable("x") int x,
            @PathVariable("y") int y) throws IOException, UnknownContentTypeException, TransparentProxyException, URISyntaxException, DataRepositoryException, PostProcessingException {
        return proxyMapProxyTileRequest(getOnlineResource(file, index), layer, z, x, y);
    }
    
    @RequestMapping (value    = "history/{revision}/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png",
                     method   = RequestMethod.GET)
    @ResponseBody
    public TransparentProxy proxyMapProxyTileRequest(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer,
            @PathVariable("z") int z,
            @PathVariable("x") int x,
            @PathVariable("y") int y) throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        return proxyMapProxyTileRequest(getOnlineResource(revision, file, index), layer, z, x, y); 
    }
    
    private TransparentProxy proxyMapProxyTileRequest(OnlineResource onlineResource, String layer, int z, int x, int y) throws URISyntaxException {
        WmsCapabilities wmsCapabilities = getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
        String url = tmsToWmsGetMapService.getWMSMapRequest(wmsCapabilities.getDirectMap(), layer, z, x, y);
        String rewritten = mapServerDetailsService.rewriteToLocalWmsRequest(url);
        return new TransparentProxy(rewritten, MediaType.IMAGE_PNG);  
    }
    
    @RequestMapping (value    = "documents/{file}/onlineResources/{index}/{layer}/legend",
                     method   = RequestMethod.GET)
    @ResponseBody
    public TransparentProxy getMapLayerLegend(
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer) throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        return getMapLayerLegend(getOnlineResource(file, index), layer);
    }
    
    @RequestMapping (value    = "history/{revision}/{file}/onlineResources/{index}/{layer}/legend",
                     method   = RequestMethod.GET)
    @ResponseBody
    public TransparentProxy getMapLayerLegend(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer) throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        return getMapLayerLegend(getOnlineResource(revision, file, index), layer);  
    }
    
    private TransparentProxy getMapLayerLegend(OnlineResource onlineResource, String layer) throws URISyntaxException {
        WmsCapabilities wmsCapabilities = getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
        for(Layer wmsLayer : wmsCapabilities.getLayers()) {
            if(wmsLayer.getName().equals(layer)) {
                if(wmsLayer.getLegendUrl() != null) {
                    String rewritten = mapServerDetailsService.rewriteToLocalWmsRequest(wmsLayer.getLegendUrl());
                    return new TransparentProxy(rewritten, MediaType.parseMediaType("image/*"));
                }
                else {
                    throw new LegendGraphicMissingException("No legend graphic is present for this layer");
                }
            }
        }
        throw new ResourceNotFoundException("The layer: " + layer + " is not present in the given service" );
    }
    
    protected OnlineResource getOnlineResource(String file, int index) throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        List<OnlineResource> onlineResources = getOnlineResources(file);
        if(index < 0 || onlineResources.size() <= index) {
            throw new ResourceNotFoundException("No online resource exists on this document at index " + index);
        }
        else {
            return onlineResources.get(index);
        }
    }
    
    protected OnlineResource getOnlineResource(String revision, String file, int index) throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        List<OnlineResource> onlineResources = getOnlineResources(revision, file);
        if(index < 0 || onlineResources.size() <= index) {
            throw new ResourceNotFoundException("No online resource exists on this document at index " + index);
        }
        else {
            return onlineResources.get(index);
        }
    }
}
