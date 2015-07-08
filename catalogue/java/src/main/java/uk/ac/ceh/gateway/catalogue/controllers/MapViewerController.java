package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The following simple controller just hands off a request to on of our 
 * templates
 * @author cjohn
 */
@Controller
public class MapViewerController {
    @RequestMapping (value = "maps",
                     method = RequestMethod.GET)
    public String loadMapViewer() {
        return "/html/mapviewer.html.tpl";
    }
}
