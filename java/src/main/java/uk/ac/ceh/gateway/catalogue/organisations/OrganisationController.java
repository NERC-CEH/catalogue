package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@ToString
public class OrganisationController {

    private final OrganisationSolrQueryService organisationService;

    public OrganisationController(OrganisationSolrQueryService organisationService) {
        this.organisationService = organisationService;
        log.info("Creating");
    }

    /** The organisation with a ROR identifier, as a list of one, or none: under the search's path, which the proxies already route */
    @GetMapping(value = "organisation/names", params = "id")
    public List<Organisation> getOrganisation(@RequestParam("id") String id) throws SolrServerException {
        return organisationService.get(id);
    }

    @GetMapping(value = "organisation/names", params = "!id")
    public List<Organisation> getOrganisations(
            @RequestParam(value = "query", defaultValue = "*") String query
    ) throws SolrServerException {
        return organisationService.query(query);
    }
}
