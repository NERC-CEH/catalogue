package uk.ac.ceh.gateway.catalogue.controllers;

import com.google.common.base.Strings;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.elements.OnlineResource.Type;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.IllegalOgcRequestTypeException;
import uk.ac.ceh.gateway.catalogue.model.NoSuchOnlineResourceException;
import uk.ac.ceh.gateway.catalogue.model.NotAGetCapabilitiesResourceException;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
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
    private final BundledReaderService<GeminiDocument> documentBundleReader;
    private final RestTemplate rest;
    
    @Autowired
    public OnlineResourceController(DataRepository<CatalogueUser> repo,
                                    CloseableHttpClient httpClient,
                                    BundledReaderService<GeminiDocument> documentBundleReader,
                                    RestTemplate rest) {
        this.repo = repo;
        this.httpClient = httpClient;
        this.documentBundleReader = documentBundleReader;
        this.rest = rest;
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
                return getWmsCapabilities(onlineResource);
            default: 
                return new RedirectView(onlineResource.getUrl());
        }
    }
    
    @RequestMapping (value = "documents/{file}/onlineResources/{index}/{type}",
                     method = RequestMethod.GET)
    public void proxyDirectServiceRequest(
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("type") String type,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, UnknownContentTypeException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        proxyDirectServiceRequest(latestRev.getRevisionID(), file, index, type, request, response);
    }
    
    @RequestMapping (value = "history/{revision}/{file}/onlineResources/{index}/{type}",
                     method = RequestMethod.GET)
    public void proxyDirectServiceRequest(
            @PathVariable("revision") String revision,
            @PathVariable("file") String file,
            @PathVariable("index") int index,
            @PathVariable("type") String type,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, UnknownContentTypeException {
        
        WmsCapabilities wmsCapabilities = getWmsCapabilities(getOnlineResource(file, revision, index));
        
        String query = request.getQueryString();
        switch (type) {
            case "wms":
                proxy(wmsCapabilities.getDirectMap(), query, response);
                break;
            case "feature":
                proxy(wmsCapabilities.getDirectFeatureInfo(), query, response );
                break;
            default:
                throw new IllegalOgcRequestTypeException("I don't understand the type: " + type);
        }
    }
    
    protected OnlineResource getOnlineResource(String file, String revision, int index) throws IOException, UnknownContentTypeException {
        return getOnlineResource(documentBundleReader.readBundle(file, revision), index);
    }
    
    protected OnlineResource getOnlineResource(GeminiDocument document, int index) {
        List<OnlineResource> onlineResources = document.getOnlineResources();
        if(index < 0 || onlineResources.size() <= index) {
            throw new NoSuchOnlineResourceException("No online resource exists on this document at index " + index);
        }
        else {
            return onlineResources.get(index);
        }
    }
    
    protected WmsCapabilities getWmsCapabilities(OnlineResource resource) {
        if(resource.getType().equals(Type.GET_CAPABILITIES)) {
            return rest.getForEntity(resource.getUrl(), WmsCapabilities.class).getBody();
        }
        else {
            throw new NotAGetCapabilitiesResourceException("The specified online resource does not represent a get capabilities");
        }
    }
    
    private void proxy(String url, String queryString, HttpServletResponse servletResponse) throws IOException {
        // The WMS Standard states that urls must end in either a ? or a &
        // If not, this method will create an invalid url
        HttpGet httpget = new HttpGet(url + Strings.nullToEmpty(queryString));
        
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
