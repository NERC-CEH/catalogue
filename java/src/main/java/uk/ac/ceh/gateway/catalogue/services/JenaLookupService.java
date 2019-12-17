package uk.ac.ceh.gateway.catalogue.services;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

/**
 * A simple lookup service powered by the jena linking database. This just looks
 * up any literals associated to a given uri
 */
@AllArgsConstructor
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
     * Metadata records (in other catalogues) linked to this record.
     * @param uri of this metadata record
     * @return list of identifiers
     */
    public List<String> linked(String uri) {
        return lookupPropertyOfSubject(uri, SOURCE, IDENTIFIER)
            .stream()
            .map(l -> l.getString())
            .filter(l -> !l.startsWith("CEH:EIDC:"))
            .filter(l -> !l.startsWith("doi:"))
            .filter(l -> !l.startsWith("http"))
            .collect(Collectors.toList());
    }
    
    /**
     * ModelApplications linked to this Model
     * @param uri of model
     * @return list of Links to modelApplications
     */
    public List<Link> modelApplications(String uri) {
        return links(uri, "PREFIX dc: <http://purl.org/dc/terms/> SELECT ?node ?title ?type ?rel WHERE {?node ?rel ?me; dc:references ?me; dc:title ?title; dc:type ?type; dc:type 'modelApplication'}");
    }
    
    /**
     * Models linked to this ModelApplication
     * @param uri of modelApplication
     * @return list of Links to models
     */
    public List<Link> models(String uri) {
        return links(uri, "PREFIX dc: <http://purl.org/dc/terms/> SELECT ?node ?title ?type ?rel WHERE { ?me ?rel ?node; dc:references ?node. ?node dc:title ?title; dc:type ?type; dc:type 'model'}");
    }
    
    public List<Link> datasets(String uri) {
        return links(uri, "PREFIX dc: <http://purl.org/dc/terms/> SELECT ?node ?title ?type ?rel WHERE {{{?me ?rel ?node; dc:references ?node.}UNION{?node ?rel ?me; dc:references ?me.} ?node dc:title ?title; dc:type ?type; dc:type 'dataset'.}UNION{?me ?rel ?node; dc:references ?node. ?node dc:source _:n . _:n dc:title ?title; dc:type ?type; dc:type 'dataset'.}}");
    }

    public List<Link> relationships(String uri, String relation) {
        String sparql = "PREFIX dc: <http://purl.org/dc/terms/> SELECT ?node ?title ?type ?rel WHERE {?me ?rel ?node; ?relation ?node . ?node dc:title ?title; dc:type ?type.} ORDER BY ?title";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        pss.setIri("relation", relation);
        return links(pss);
    }

    public List<Link> inverseRelationships(String uri, String relation) {
        String sparql = "PREFIX dc: <http://purl.org/dc/terms/> SELECT ?node ?title ?type ?rel WHERE {?node ?rel ?me; ?relation ?me . ?node dc:title ?title; dc:type ?type.} ORDER BY ?title";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        pss.setIri("relation", relation);
        return links(pss);
    }

    public List<Link> allRelatedRecords(String uri) {
        String sparql = "PREFIX dc: <http://purl.org/dc/terms/> SELECT ?node ?rel ?title ?type WHERE {{?me ?rel ?node. ?node dc:title ?title; dc:type ?type.} UNION {?node ?rel ?me. ?node dc:title ?title; dc:type ?type.}FILTER(REGEX(STR(?rel),'^http://vocabs.ceh.ac.uk/eidc#'))}";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        return links(pss);
    }


    /**
     * This finds the most recent version of a superseded resource
     * i.e. if a superseded recource is iteself superseded, it will return 
     * only the last in the chain
     */
    public List<Link> superseded(String uri) {
        String sparql = "PREFIX dc: <http://purl.org/dc/terms/> PREFIX : <http://vocabs.ceh.ac.uk/eidc#> SELECT DISTINCT ?node ?type ?title ?rel WHERE {?node :supersedes+ ?me; dc:title ?title; dc:type ?type.BIND( :supersedes as ?rel)FILTER (!EXISTS {?x :supersedes ?node})}";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        return links(pss);
    }

    public Link metadata(String id) {
        id = nullToEmpty(id);
        String sparql = "PREFIX dc: <http://purl.org/dc/terms/> SELECT ?node ?title ?type ?rel WHERE {?node dc:identifier ?id; dc:title ?title; dc:type ?type. BIND(<https://vocabs.ceh.ac.uk/eidc#> as ?rel)}";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);  
        pss.setLiteral("id", id);
        return links(pss).stream().findFirst().orElse(null);
    }
    
    private List<Link> links(@NonNull String uri, String sparql) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);  
        pss.setIri("me", uri);
        return links(pss);
    }

    private List<Link> links(ParameterizedSparqlString pss) {
        List<Link> toReturn = new ArrayList<>();
        jenaTdb.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec.execSelect().forEachRemaining(s -> {
                toReturn.add(
                    Link.builder()
                        .title(s.getLiteral("title").getString())
                        .href(s.getResource("node").getURI())
                        .associationType(s.getLiteral("type").getString())
                        .rel(s.getResource("rel").getURI())
                        .build()
                );
            });
        } finally {
            jenaTdb.end();
        }
        return toReturn;
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
            "SELECT ?attr WHERE { ?uri ?relationship ?attr }"
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
    
    /**
     * Lookup a resource (the subject) and return a property of that subject. 
     * @param objectUri uri of resource (the object)
     * @param relationshipToSubject uri of relationship to subject
     * @param relationshipOnSubject uri of literal on subject
     * @return 
     */
    public List<Literal> lookupPropertyOfSubject(
        String objectUri,
        Property relationshipToSubject,
        Property relationshipOnSubject
    ) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
            "SELECT ?attr WHERE { _:s ?relationshipToSubject ?objectUri ; ?relationshipOnSubject ?attr . }"
        );
        pss.setParam("objectUri", createResource(objectUri));
        pss.setParam("relationshipToSubject", relationshipToSubject);
        pss.setParam("relationshipOnSubject", relationshipOnSubject);
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
