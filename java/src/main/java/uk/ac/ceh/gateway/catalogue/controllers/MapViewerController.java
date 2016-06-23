package uk.ac.ceh.gateway.catalogue.controllers;

import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

/**
 * The following simple controller just hands off a request to on of our 
 * templates
 * @author cjohn
 */
@Controller
@RequestMapping(value="maps")
public class MapViewerController {
    private final MapServerDetailsService mapServerDetailsService;
    
    @Autowired
    public MapViewerController(MapServerDetailsService mapServerDetailsService) {
        this.mapServerDetailsService = mapServerDetailsService;
    }
   
    @RequestMapping(method = RequestMethod.GET)
    public String loadMapViewer() {
        return "/html/mapviewer.html.tpl";
    }
    
    @RequestMapping (value = "{file}")
    @ResponseBody
    public TransparentProxy wmsService(
            @PathVariable("file") String file,
            HttpServletRequest request) throws URISyntaxException {
        return new TransparentProxy(mapServerDetailsService.getLocalWMSRequest(file, request.getQueryString()));
    }
    
}
