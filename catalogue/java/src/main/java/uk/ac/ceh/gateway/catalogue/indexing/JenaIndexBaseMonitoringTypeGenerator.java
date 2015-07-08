package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Link;
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
//                createRelationships(links, me, facility.getNarrowerThan(), BROADER,    INCOMING);
//                createRelationships(links, me, facility.getBroaderThan(),  BROADER,    OUTGOING);
//                createRelationships(links, me, facility.getBelongsTo(),    BELONGS_TO, OUTGOING);
                createRelationships(links, me, facility.getRelatedTo(),    RELATED_TO);
            } else if (baseMonitoringType instanceof Network) {
                Network network = (Network)baseMonitoringType;
                createRelationships(links, me, network.getInvolvedIn(),   INVOLVED_IN);
                createRelationships(links, me, network.getSupersedes(),   SUPERSEDES);
                createRelationships(links, me, network.getSupersededBy(), SUPERSEDED_BY);
//                createRelationships(links, me, network.getNarrowerThan(), BROADER,    INCOMING);
//                createRelationships(links, me, network.getBroaderThan(),  BROADER,    OUTGOING);
//                createRelationships(links, me, network.getContains(),     BELONGS_TO, INCOMING);
            } else if (baseMonitoringType instanceof Programme) {
                Programme programme = (Programme)baseMonitoringType;
                createRelationships(links, me, programme.getTriggers(),     TRIGGERS);
                createRelationships(links, me, programme.getSupersedes(),   SUPERSEDES);
                createRelationships(links, me, programme.getSupersededBy(), SUPERSEDED_BY);
//                createRelationships(links, me, programme.getNarrowerThan(), BROADER,    INCOMING);
//                createRelationships(links, me, programme.getBroaderThan(),  BROADER,    OUTGOING);
            }
        }
        return links;
    }
    
    private void createRelationships(List<Statement> statements, Resource id, List<Link> links, Property property) {
        statements.addAll(Optional.ofNullable(links)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(Link::getHref)
                .filter(Objects::nonNull)
                .map( l -> statement(id, property, l))
                .collect(Collectors.toList()));  
    }
    
    private static Statement statement(Resource id, Property property, String link) {
        return ResourceFactory.createStatement(id, property, ResourceFactory.createResource(link));
    }
}
