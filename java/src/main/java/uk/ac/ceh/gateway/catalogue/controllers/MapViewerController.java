package uk.ac.ceh.gateway.catalogue.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.MAPSERVER_GML_VALUE;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

/**
 * The following simple controller just hands off a request to on of our 
 * templates
 */
@CrossOrigin
@Controller
@RequestMapping(value="maps")
public class MapViewerController {
    private final static String INFO_FORMAT = "INFO_FORMAT";
    private final RestTemplate rest;
    private final MapServerDetailsService mapServerDetailsService;
    
    @Autowired
    public MapViewerController(RestTemplate rest, MapServerDetailsService mapServerDetailsService) {
        this.rest = rest;
        this.mapServerDetailsService = mapServerDetailsService;
    }
   
    @RequestMapping(method = RequestMethod.GET)
    public String loadMapViewer() {
        return "/html/mapviewer.ftl";
    }
    
    @RequestMapping (value = "{file}")
    @ResponseBody
    public Object wmsService(
            @PathVariable("file") String file,
            HttpServletRequest request) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(request.getQueryString(), StandardCharsets.UTF_8, '&');
        
        if(isLocalGetFeatureInfoRequest(params)) {
            String query = createQueryStringWithLocalInfoFormat(params, MAPSERVER_GML_VALUE);
            URI url = new URI(mapServerDetailsService.getLocalWMSRequest(file, query));
            return rest.getForObject(url, WmsFeatureInfo.class);
        }
        return new TransparentProxy(mapServerDetailsService.getLocalWMSRequest(file, request.getQueryString()));
    }
    
    public String createQueryStringWithLocalInfoFormat(List<NameValuePair> orig, String format) {
        List<NameValuePair> params = orig.stream()
                .filter((p) -> !p.getName().equalsIgnoreCase(INFO_FORMAT))
                .collect(Collectors.toList());
        params.add(new BasicNameValuePair("INFO_FORMAT", format));
        return URLEncodedUtils.format(params, StandardCharsets.UTF_8);
    }
    
    public boolean isLocalGetFeatureInfoRequest(List<NameValuePair> params) {
        return  isSetting(params, "SERVICE", "WMS") &&
                isSetting(params, "REQUEST", "GetFeatureInfo") &&
                (
                    isSetting(params, INFO_FORMAT, "text/xml") ||
                    isSetting(params, INFO_FORMAT, "application/json")
                );
    }
    
    private boolean isSetting(List<NameValuePair> params, String key, String val) {
        return params.stream()
                .filter((p) -> p.getName().equalsIgnoreCase(key))
                .anyMatch((p) -> p.getValue().equalsIgnoreCase(val));
    }
}
