package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createStatement;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import java.time.LocalDate;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType.BoundingBox;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Geometry;
import uk.ac.ceh.gateway.catalogue.ef.Link;
import uk.ac.ceh.gateway.catalogue.ef.Link.TimedLink;
import uk.ac.ceh.gateway.catalogue.ef.Network;
import uk.ac.ceh.gateway.catalogue.ef.Programme;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

/**
 * The following code is a Jena translation of the linking mechanism defined for
 * UKEOF. This is defined in uk.ac.ceh.ukeof.linkstore.guava.Linker
 * @author cjohn
 */
@Data
public class JenaIndexBaseMonitoringTypeGenerator implements IndexGenerator<BaseMonitoringType, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;
    
    @Override
    public List<Statement> generateIndex(BaseMonitoringType baseMonitoringType) throws DocumentIndexingException {
        List<Statement> links = generator.generateIndex(baseMonitoringType);
        if(baseMonitoringType.getId() != null) {
            Resource me = generator.resource(baseMonitoringType.getId());
            if (baseMonitoringType instanceof Activity) {
                Activity activity = (Activity)baseMonitoringType;
                createRelationships(links, me, activity.getSetUpFor(), SET_UP_FOR);
                createRelationships(links, me, activity.getUses(),     USES);
            } else if (baseMonitoringType instanceof Facility) {
                Facility facility = (Facility)baseMonitoringType;
                createRelationships(links, me, facility.getInvolvedIn(),   INVOLVED_IN);
                createRelationships(links, me, facility.getSupersedes(),   SUPERSEDES);
                createRelationships(links, me, facility.getSupersededBy(), SUPERSEDED_BY);
                createRelationships(links, me, facility.getNarrowerThan(), NARROWER);
                createRelationships(links, me, facility.getBroaderThan(),  BROADER);
                createRelationships(links, me, facility.getBelongsTo(),    BELONGS_TO);
                createRelationships(links, me, facility.getRelatedTo(),    RELATED_TO);
                Optional.ofNullable(facility.getGeometry()).map(Geometry::getValue).ifPresent(w -> {
                    links.add(createStatement(me, HAS_GEOMETRY, createTypedLiteral(w, WKT_LITERAL)));
                });
            } else if (baseMonitoringType instanceof Network) {
                Network network = (Network)baseMonitoringType;
                createRelationships(links, me, network.getInvolvedIn(),   INVOLVED_IN);
                createRelationships(links, me, network.getSupersedes(),   SUPERSEDES);
                createRelationships(links, me, network.getSupersededBy(), SUPERSEDED_BY);
                createRelationships(links, me, network.getNarrowerThan(), NARROWER);
                createRelationships(links, me, network.getBroaderThan(),  BROADER);
                createRelationships(links, me, network.getContains(),     CONTAINS);
            } else if (baseMonitoringType instanceof Programme) {
                Programme programme = (Programme)baseMonitoringType;
                createRelationships(links, me, programme.getTriggers(),     TRIGGERS);
                createRelationships(links, me, programme.getSupersedes(),   SUPERSEDES);
                createRelationships(links, me, programme.getSupersededBy(), SUPERSEDED_BY);
                createRelationships(links, me, programme.getNarrowerThan(), NARROWER);
                createRelationships(links, me, programme.getBroaderThan(),  BROADER);
            }
            Optional.ofNullable(baseMonitoringType.getBoundingBoxes())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(BoundingBox::getWkt)
                    .forEach( w -> {
                        links.add(createStatement(me, HAS_GEOMETRY, createTypedLiteral(w, WKT_LITERAL)));
                    });
        }
        return links;
    }
    
    private void createRelationships(List<Statement> statements, Resource id, List<? extends Link> links, Property property) {
        statements.addAll(Optional.ofNullable(links)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map( l -> linkStatements(id, property, l))
                .flatMap(l -> l.stream())
                .collect(Collectors.toList()));  
    }
    
    /**
     * For the given Link create a set of statements which represents the details 
     * of it and define its relationship to the provided resource via the relation
     * property
     * @param resource which the link has come off
     * @param relation of how this link is related to the resource
     * @param link to expand and define semantically
     * @see uk.ac.ceh.gateway.catalogue.postprocess.BaseMonitoringTypePostProcessingService#withLinks(com.hp.hpl.jena.rdf.model.Property, com.hp.hpl.jena.rdf.model.Resource) 
     * @return a list of statements representing this linking
     */
    private List<Statement> linkStatements(Resource resource, Property relation, Link link) {
        List<Statement> statements = new ArrayList<>();
        
        Resource linkResource = createResource(link.getHref());
        // If there is no time span defined then just link as if it is a normal link
        if(isTimedLink(link)) {
            TimedLink timedLink = (TimedLink)link;
            Resource timedLinkUri = createResource();
            statements.add(createStatement(resource, relation, timedLinkUri));
            statements.add(createStatement(timedLinkUri, IDENTIFIER, linkResource));

            Resource linkingTime = createResource();
            Optional.ofNullable(timedLink.getLinkingTime().getStart())
                    .ifPresent( s -> statements.add(createStatement(linkingTime, TEMPORAL_BEGIN, dateTime(s))));
            Optional.ofNullable(timedLink.getLinkingTime().getEnd())
                    .ifPresent( s -> statements.add(createStatement(linkingTime, TEMPORAL_END, dateTime(s))));

            statements.add(createStatement(timedLinkUri, LINKING_TIME, linkingTime));
        }
        else {
            //There is no timespan associated to this link, create a normal connection
            statements.add(createStatement(resource, relation, linkResource));
        }
        return statements;
    }
    
    /**
     * Determines if this link should be treated as a timed link when indexing
     * @param link to check if it has a lifespan defined
     * @return if this should be considered a timed link
     */
    private boolean isTimedLink(Link link) {
        if(link instanceof TimedLink) {
            TimedLink timed = (TimedLink)link;
            return timed.getLinkingTime() != null && (
                    timed.getLinkingTime().getStart() != null ||
                    timed.getLinkingTime().getEnd() != null
                );
        }
        return false;
    }
    
    // Turn the given localdate time into a semantic time literal
    private static Literal dateTime(LocalDate time){ 
        return createTypedLiteral(time);
    }
 }
