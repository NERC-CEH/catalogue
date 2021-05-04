package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.MAPSERVER_GML_VALUE;

/**
 * The following simple controller just hands off a request to on of our 
 * templates
 */
@Slf4j
@ToString
@CrossOrigin
@Controller
@RequestMapping(value="maps")
public class MapViewerController {
    private final static String INFO_FORMAT = "INFO_FORMAT";
    private final RestTemplate rest;
    private final MapServerDetailsService mapServerDetailsService;

    public MapViewerController(@Qualifier("wms") RestTemplate rest, MapServerDetailsService mapServerDetailsService) {
        this.rest = rest;
        this.mapServerDetailsService = mapServerDetailsService;
        log.info("Creating {}", this);
    }
   
    @GetMapping
    public String loadMapViewer() {
        return "/html/mapviewer";
    }
    
    @RequestMapping(value = "{file}")
    @ResponseBody
    @SneakyThrows
    public Object wmsService(
            @PathVariable("file") String file,
            HttpServletRequest request
    ) {
        List<NameValuePair> params = URLEncodedUtils.parse(request.getQueryString(), StandardCharsets.UTF_8, '&');
        if (log.isDebugEnabled()) {
            params.forEach(param -> log.debug("{} - {}", param.getName(), param.getValue()));
        }
        
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
