package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.NoSuchOnlineResourceException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.MapProxyService;
import uk.ac.ceh.gateway.catalogue.services.MapProxyServiceException;
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
    private final MapProxyService mapProxyFactoryService;
    
    @Autowired
    public OnlineResourceController(DataRepository<CatalogueUser> repo,
                                    CloseableHttpClient httpClient,
                                    BundledReaderService<MetadataDocument> documentBundleReader,
                                    GetCapabilitiesObtainerService getCapabilitiesObtainerService,
                                    MapProxyService mapProxyFactoryService) {
        this.repo = repo;
        this.httpClient = httpClient;
        this.documentBundleReader = documentBundleReader;
        this.getCapabilitiesObtainerService = getCapabilitiesObtainerService;
        this.mapProxyFactoryService = mapProxyFactoryService;
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
            case GET_CAPABILITIES : 
                return getCapabilitiesObtainerService.getWmsCapabilities(onlineResource);
            default: 
                return new RedirectView(onlineResource.getUrl());
        }
    }
    
    @RequestMapping (value = "documents/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png",
                     method = RequestMethod.GET)
    public void proxyDirectServiceRequest(
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer,
            @PathVariable("z") int z,
            @PathVariable("x") int x,
            @PathVariable("y") int y,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, UnknownContentTypeException, MapProxyServiceException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        proxyDirectServiceRequest(latestRev.getRevisionID(), file, index, layer, z, x, y, request, response);
    }
    
    @RequestMapping (value = "history/{revision}/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png",
                     method = RequestMethod.GET)
    public void proxyDirectServiceRequest(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("layer") String layer,
            @PathVariable("z") int z,
            @PathVariable("x") int x,
            @PathVariable("y") int y,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, UnknownContentTypeException, MapProxyServiceException {
        OnlineResource resource = getOnlineResource(file, revision, index);
        String mapService = mapProxyFactoryService.getTiledMapService(resource);
        proxy(mapService, layer, z, x, y, response);
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
    
    protected void proxy(String mapService, String layer, int z, int x, int y, HttpServletResponse servletResponse) throws IOException {
        HttpGet httpget = new HttpGet("http://localhost:8080/" + mapService + "/tiles/1.0.0/" + layer + "/" + z + "/" + x + "/" + y + ".png");
        
        try (CloseableHttpResponse response = httpClient.execute(httpget)) {
            HttpEntity entity = response.getEntity();
            servletResponse.setContentType(entity.getContentType().getValue());
            copyAndClose(entity, servletResponse);
        }
    }
    
    private static void copyAndClose(HttpEntity in, HttpServletResponse response) throws IOException {
        try (ServletOutputStream out = response.getOutputStream()) {
            in.writeTo(out);
        }
    }
}
