package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

/**
 * This class will create map proxy services for given get capabilities
 * @author cjohn
 */
public class MapProxyService {
    private final File output;
    private final GetCapabilitiesObtainerService getCapabilitiesService;
    private final Template freeMarker;
    
    public MapProxyService(File output, 
            GetCapabilitiesObtainerService getCapabilitiesService
        ) throws IOException {
        this.output = output;
        this.getCapabilitiesService = getCapabilitiesService;
        
        Configuration freeMarkerConfig = new Configuration();
        freeMarkerConfig.setClassForTemplateLoading(this.getClass(), "");
        this.freeMarker = freeMarkerConfig.getTemplate("mapproxy.yaml.tpl");
    }
    
    public String getTiledMapService(OnlineResource resource) throws MapProxyServiceException {
        String mapProxyConfigName = DigestUtils.sha1Hex(resource.getUrl());
        File mapProxyConfig = new File(output, mapProxyConfigName + ".yaml");
        if(!mapProxyConfig.exists()) {
            //Create a new Map Proxy Config.yaml file
            WmsCapabilities capabilities = getCapabilitiesService.getWmsCapabilities(resource);
            try (Writer writer = new FileWriter(mapProxyConfig)) {
                Map<String, Object> model = new HashMap<>();
                model.put("capabilities", capabilities);
                model.put("id", mapProxyConfigName);
                freeMarker.process(model, writer);
            }
            catch(IOException | TemplateException ex) {
                throw new MapProxyServiceException("Failed to generate mapproxy.yaml file", ex);
            }
        }
        
        return mapProxyConfigName;
    }
}
