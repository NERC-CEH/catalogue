package uk.ac.ceh.gateway.catalogue.deims;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Slf4j
@ToString
@RequestMapping("deims")
public class DEIMSSiteSearchController {

    private DEIMSSolrQueryService DEIMSService;

   // @Autowired
    public DEIMSSiteSearchController(DEIMSSolrQueryService deimsService) {
        this.DEIMSService = deimsService;
        log.info("Creating {}", this);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public List<DeimsSite> getSites(
            @RequestParam(value = "query") String query) throws SolrServerException {
        return DEIMSService.query(query);
    }
}
