package uk.ac.ceh.gateway.catalogue.services;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.HAS_GEOMETRY;

/**
 * A simple lookup service powered by the jena linking database. This just looks
 * up any literals associated to a given uri
 * @author cjohn
 */
@Data
public class JenaLookupService {
    private final Dataset jenaTdb;
    
    /**
     * Looks up the specified uri for an attached geometry.
     * @param uri to lookup for a geometry
     * @return a list of string representations of the well known text attached
     *  to the given uri
     */
    public List<String> wkt(String uri) {
        return lookup(uri, HAS_GEOMETRY)
                .stream()
                .map(l -> l.getString())
                .collect(Collectors.toList());
    }
    
    /**
     * Performs a literal lookup from the jena database for literals associated 
     * to the given uri with a specified relationship
     * @param uri to look up an attribute of
     * @param relationship of the literal to the uri
     * @return a list of matching literals
     */
    public List<Literal> lookup(String uri, Property relationship) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
            "SELECT ?attr " +
            "WHERE { ?uri ?relationship ?attr }"
        );
        pss.setParam("uri", createResource(uri));
        pss.setParam("relationship", relationship);
        List<Literal> toReturn = new ArrayList<>();
        jenaTdb.begin(ReadWrite.READ);
        try (QueryExecution q = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            q.execSelect().forEachRemaining(s -> { toReturn.add(s.getLiteral("attr")); });
        } finally {
            jenaTdb.end();
        }
        return toReturn;
    }
}
