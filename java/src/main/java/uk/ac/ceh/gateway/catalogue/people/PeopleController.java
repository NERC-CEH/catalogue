package uk.ac.ceh.gateway.catalogue.people;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

/**
 * The editor's people look-up (dri-one #534): the people the catalogue already names with an ORCID,
 * and whose an ORCID iD is, as ORCID's registry says. For signed-in editors only. Nobody else edits a
 * record, and each check of an iD spends ORCID's daily allowance for our address.
 *
 * <p>{@code /people/**} must be in the eidc and INMS proxies' path allow-lists, or the proxy answers
 * for the app with its own 404.
 */
@Slf4j
@RestController
public class PeopleController {

    private final PeopleSearchService peopleSearchService;
    private final OrcidRegistry orcidRegistry;

    public PeopleController(PeopleSearchService peopleSearchService, OrcidRegistry orcidRegistry) {
        this.peopleSearchService = peopleSearchService;
        this.orcidRegistry = orcidRegistry;
    }

    @GetMapping("people/names")
    public ResponseEntity<List<KnownPerson>> names(
        @ActiveUser CatalogueUser user,
        @RequestParam(value = "query", defaultValue = "") String query
    ) throws SolrServerException {
        if (user == null || user.isPublic()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(peopleSearchService.search(query, user));
    }

    /**
     * Whose an iD is: 200 with the person, 404 for an iD ORCID has never issued, 400 for one that
     * can't be an iD (a check character that's wrong), and 502 when ORCID can't be asked.
     */
    @GetMapping("people/orcid")
    public ResponseEntity<OrcidPerson> orcid(
        @ActiveUser CatalogueUser user,
        @RequestParam("id") String id
    ) {
        if (user == null || user.isPublic()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        var orcid = Orcid.id(id);
        if (orcid.isEmpty()) return ResponseEntity.badRequest().build();
        try {
            return orcidRegistry.person(orcid.get())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (OrcidRegistry.OrcidUnavailableException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
