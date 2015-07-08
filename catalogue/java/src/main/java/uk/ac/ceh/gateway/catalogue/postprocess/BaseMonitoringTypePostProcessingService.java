package uk.ac.ceh.gateway.catalogue.postprocess;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Link;
import uk.ac.ceh.gateway.catalogue.ef.Network;
import uk.ac.ceh.gateway.catalogue.ef.Programme;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

/**
 * Defines a post processing service which can be used adding additional 
 * information to a BaseMonitoringType.
 * 
 * The logic in here is based upon: uk.ac.ceh.ukeof.linkstore.guava.LinkWrapper
 * @author cjohn
 */
@Data
public class BaseMonitoringTypePostProcessingService implements PostProcessingService<BaseMonitoringType> {
    private final static boolean HAS_INCOMING = true, HAS_OUTGOING = false;
    private final Dataset jenaTdb;
    
    @Override
    public void postProcess(BaseMonitoringType document) {
        Resource uri = ResourceFactory.createResource(document.getUri().toString());
        if (document instanceof Activity) {
            Activity activity = (Activity)document;
            appendLinks(activity.getSetUpFor(), withLinksWhere(uri, HAS_OUTGOING, SET_UP_FOR));
            appendLinks(activity.getUses(), withLinksWhere(uri, HAS_OUTGOING, USES));
        } else if (document instanceof Facility) {
            Facility facility = (Facility)document;
            appendLinks(facility.getInvolvedIn(), withLinksWhere(uri, HAS_INCOMING, USES));
            appendLinks(facility.getSupersedes(), withLinksWhere(uri, HAS_OUTGOING, SUPERSEDES));
            appendLinks(facility.getSupersededBy(), withLinksWhere(uri, HAS_INCOMING, SUPERSEDES));
//            facility.setNarrowerThan(joinedLinks(facility.getNarrowerThan(), links(uri, BROADER, INCOMING)));
//            facility.setBroaderThan(joinedLinks(facility.getBroaderThan(), links(uri, BROADER, OUTGOING)));
//            facility.setBelongsTo(joinedLinks(facility.getBelongsTo(), links(uri, BELONGS_TO, OUTGOING)));
            appendLinks(facility.getRelatedTo(), withLinksWhere(uri, HAS_OUTGOING, RELATED_TO));
        } else if (document instanceof Network) {
            Network network = (Network)document;            
            appendLinks(network.getInvolvedIn(), withLinksWhere(uri, HAS_INCOMING, USES));
            appendLinks(network.getSupersedes(), withLinksWhere(uri, HAS_OUTGOING, SUPERSEDES));
            appendLinks(network.getSupersededBy(), withLinksWhere(uri, HAS_INCOMING, SUPERSEDES));
//            network.setNarrowerThan(joinedLinks(network.getNarrowerThan(), links(uri, BROADER, INCOMING)));
//            network.setBroaderThan(joinedLinks(network.getBroaderThan(), links(uri, BROADER, OUTGOING)));
//            network.setContains(joinedLinks(network.getContains(), links(uri, BELONGS_TO, INCOMING)));
        } else if (document instanceof Programme) {
            Programme programme = (Programme)document;
            appendLinks(programme.getTriggers(), withLinksWhere(uri, HAS_INCOMING, SET_UP_FOR));
            appendLinks(programme.getSupersedes(), withLinksWhere(uri, HAS_OUTGOING, SUPERSEDES));
            appendLinks(programme.getSupersededBy(), withLinksWhere(uri, HAS_INCOMING, SUPERSEDES));
//            programme.setNarrowerThan(joinedLinks(programme.getNarrowerThan(), links(uri, BROADER, OUTGOING)));
//            programme.setBroaderThan(joinedLinks(programme.getBroaderThan(), links(uri, BROADER, INCOMING)));
        }
    }
    
    /**
     * The following method adds all of the links in B to A where the hrefs don't
     * match.
     * @param a List which will contain all of the processed links
     * @param b The list with prospective links to add
     */
    private void appendLinks(List<Link> a, List<Link> b) {
        List<String> hrefs = a.stream().map(Link::getHref).collect(Collectors.toList());
        b.removeIf(e -> hrefs.contains(e.getHref()));
        a.addAll(b);
    }
    
    /**
     * Get the links which this relationship
     * 
     * @param relationship
     * @param doc
     * @param incoming
     * @return 
     */
    private List<Link> withLinksWhere(Resource doc, boolean incoming, Property relationship) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
                "SELECT ?link ?title WHERE {" + (
                    (incoming) ? "?link ?relationship ?doc . " 
                               : "?doc ?relationship ?link . ")+
                "?link <http://purl.org/dc/terms/title> ?title . }");
        pss.setParam("doc", doc);
        pss.setParam("relationship", relationship);
        List<Link> toReturn = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec.execSelect().forEachRemaining(s -> {
                toReturn.add(new Link()
                        .setHref(s.getResource("link").getURI())
                        .setTitle(s.getLiteral("title").getString())
                );
            });
        }
        return toReturn;
    }
}
