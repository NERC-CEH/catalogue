package uk.ac.ceh.gateway.catalogue.deims;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Profile("server:elter")
@Controller
@Slf4j
@ToString
@RequestMapping("vocabulary")
public class DeimsSiteSearchController {

    private final DeimsSolrQueryService deimsService;

    public DeimsSiteSearchController(DeimsSolrQueryService deimsService) {
        this.deimsService = deimsService;
        log.info("Creating");
    }

    @GetMapping(value = "deims")
    @ResponseBody
    public List<DeimsSolrIndex> getSites(
            @RequestParam(value = "query", defaultValue = "*") String query
    ) throws SolrServerException {
        return deimsService.query(query);
    }
}
