package uk.ac.ceh.gateway.catalogue.graph;

import java.util.Map;

import com.google.common.collect.Maps;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@Controller
@AllArgsConstructor
public class GraphController {

    private GraphService graphService;
    private DocumentRepository documentRepository;

    public void getGraph() {}

    @ResponseBody
    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "network/{file}", method = RequestMethod.GET)   
    public ModelAndView network(@ActiveUser CatalogueUser user, @PathVariable("file") String file) {
        Map<String, Object> model = Maps.newHashMap();
        val document = documentRepository.read(file);
        model.put("document", document);
        return new ModelAndView("/html/network.ftl", model);
    }

    @ResponseBody
    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "graph/{file}", method = RequestMethod.GET)   
    public ResponseEntity<Graph> graph(@ActiveUser CatalogueUser user, @PathVariable("file") String file) {
        val document = documentRepository.read(file);
        val graph = graphService.shallow(
            document.getId(),
            document.getTitle(),
            document.getUri()
        );
        return ResponseEntity.ok(graph);   
    }
}