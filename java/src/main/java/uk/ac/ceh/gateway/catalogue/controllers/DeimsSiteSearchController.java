package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrIndex;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrQueryService;

import java.util.List;

@Profile("elter")
@Controller
@Slf4j
@ToString
public class DeimsSiteSearchController {

    private final DeimsSolrQueryService deimsService;

    public DeimsSiteSearchController(DeimsSolrQueryService deimsService) {
        this.deimsService = deimsService;
        log.info("Creating");
    }

    @GetMapping(value = "elter/vocabulary/deims")
    @ResponseBody
    public List<DeimsSolrIndex> getSites(
            @RequestParam(value = "query", defaultValue = "*") String query
    ) throws SolrServerException {
        return deimsService.query(query);
    }
}
