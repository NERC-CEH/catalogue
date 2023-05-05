package uk.ac.ceh.gateway.catalogue.postprocess;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import uk.ac.ceh.gateway.catalogue.ef.*;
import uk.ac.ceh.gateway.catalogue.ef.Link.TimedLink;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * Defines a post-processing service which can be used adding additional
 * information to a BaseMonitoringType.
 *
 * The logic in here is based upon: uk.ac.ceh.ukeof.linkstore.guava.LinkWrapper
 */
@Slf4j
@ToString(exclude = "jenaTdb")
public class BaseMonitoringTypePostProcessingService implements PostProcessingService<BaseMonitoringType> {
    private final Dataset jenaTdb;

    public BaseMonitoringTypePostProcessingService(Dataset jenaTdb) {
        this.jenaTdb = jenaTdb;
        log.info("Creating {}", this);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void postProcess(BaseMonitoringType document) {
        Resource uri = ResourceFactory.createResource(document.getUri());
        jenaTdb.begin(ReadWrite.READ);
        try {
            if (document instanceof Activity) {
                Activity activity = (Activity)document;
                appendLinks(activity.getSetUpFor(), withLinks(TRIGGERS, uri));
                appendLinks(activity.getUses(), withLinks(INVOLVED_IN, uri));
            } else if (document instanceof Facility) {
                Facility facility = (Facility)document;
                appendLinks(facility.getInvolvedIn(), withLinks(USES, uri));
                appendLinks(facility.getSupersedes(), withLinks(SUPERSEDED_BY, uri));
                appendLinks(facility.getSupersededBy(), withLinks(SUPERSEDES, uri));
                appendLinks(facility.getNarrowerThan(), withLinks(BROADER, uri));
                appendLinks(facility.getBroaderThan(), withLinks(NARROWER, uri));
                appendLinks(facility.getBelongsTo(), withLinks(CONTAINS, uri));
                appendLinks(facility.getRelatedTo(), withLinks(RELATED_TO, uri));
            } else if (document instanceof Network) {
                Network network = (Network)document;
                appendLinks(network.getInvolvedIn(), withLinks(USES, uri));
                appendLinks(network.getSupersedes(), withLinks(SUPERSEDED_BY, uri));
                appendLinks(network.getSupersededBy(), withLinks(SUPERSEDES, uri));
                appendLinks(network.getNarrowerThan(), withLinks(BROADER, uri));
                appendLinks(network.getBroaderThan(), withLinks(NARROWER, uri));
                appendLinks(network.getContains(), withLinks(BELONGS_TO, uri));
            } else if (document instanceof Programme) {
                Programme programme = (Programme)document;
                appendLinks(programme.getTriggers(), withLinks(SET_UP_FOR, uri));
                appendLinks(programme.getSupersedes(), withLinks(SUPERSEDED_BY, uri));
                appendLinks(programme.getSupersededBy(), withLinks(SUPERSEDES, uri));
                appendLinks(programme.getNarrowerThan(), withLinks(BROADER, uri));
                appendLinks(programme.getBroaderThan(), withLinks(NARROWER, uri));
            }
        } finally {
            jenaTdb.end();
        }
    }

    /**
     * The following method will add the elements of b into a when there is no
     * matching element in a.
     * @param a List which will contain all of the processed links
     * @param b The list with prospective links to add
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void appendLinks(List a, List b) {
        b.stream().filter(e -> !a.contains(e)).forEach(a::add);
    }

    /**
     * Links in EF documents can be quite complicated. For the normal links, the
     * generated triples are as expected. E.g.
     *
     *          <Activity_URI> -> <Related_To> -> <Facility _URI>
     *
     * However timed links are a different story, these look more like this:
     *
     *                                 <Start> -> "20-10-12"
     *                               /
     *  <Activity_URI> -> <Triggers> --> <End>   -> "24-10-30"
     *                               \
     *                                 <Identifier> -> <Facility_URI>
     *
     * The code here will create a collection of links for both types of graph,
     * where the type is either Link or TimedLink based upon the structure.
     * @param relationship The back relationship between the supplied doc
     * @param doc uri to search for
     * @return a list of links which link to the given doc by the specified
     *  relationship
     */
    private List<Link> withLinks(Property relationship, Resource doc) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
            "SELECT ?link ?title ?start ?end " +
            "WHERE {{" +
            "    ?link ?relationship ?doc ." +
            "    MINUS { ?link <http://purl.org/voc/ef#linkingTime> ?time . } ." +
            "    OPTIONAL { ?link <http://purl.org/dc/terms/title> ?title . } ." +
            "  } " +
            "  UNION {" +
            "    ?timingLink ?relationship ?doc ." +
            "    ?timingLink <http://purl.org/dc/terms/identifier> ?link . " +
            "    ?timingLink <http://purl.org/voc/ef#linkingTime> ?time ." +
            "    OPTIONAL { ?link <http://purl.org/dc/terms/title> ?title . } . " +
            "    OPTIONAL { ?time <http://def.seegrid.csiro.au/isotc211/iso19108/2002/temporal#begin> ?start } . " +
            "    OPTIONAL { ?time <http://def.seegrid.csiro.au/isotc211/iso19108/2002/temporal#end> ?end }" +
            "}}"
        );
        pss.setParam("doc", doc);
        pss.setParam("relationship", relationship);
        List<Link> toReturn = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec.execSelect().forEachRemaining(s -> {
                Link link = (s.contains("start") || s.contains("end")) ? new TimedLink().setLinkingTime(lifespan(s)) : new Link();
                link.setHref(s.getResource("link").getURI());
                Optional.ofNullable(s.getLiteral("title")).ifPresent(t -> link.setTitle(t.getString()));
                toReturn.add(link);
            });
        }
        return toReturn;
    }

    private Lifespan lifespan(QuerySolution s) {
        Lifespan toReturn = new Lifespan();
        Optional.ofNullable(s.getLiteral("start")).ifPresent(d -> toReturn.setStart(getDate(d)));
        Optional.ofNullable(s.getLiteral("end")).ifPresent(d -> toReturn.setEnd(getDate(d)));
        return toReturn;
    }

    private LocalDate getDate(Literal d) {
      return (LocalDate) d.getValue();
    }
}
