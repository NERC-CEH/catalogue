package uk.ac.ceh.gateway.catalogue.templateHelpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * A simple lookup service powered by the jena linking database. This just looks
 * up any literals associated to a given uri
 */
@SuppressWarnings({"unused"})
@Slf4j
@ToString
@Service
public class JenaLookupService {
    private final Dataset jenaTdb;

    public JenaLookupService(@NonNull Dataset jenaTdb) {
        this.jenaTdb = jenaTdb;
        log.info("Creating");
    }

    /**
     * Looks up the specified uri for an attached geometry.
     * @param uri to lookup for a geometry
     * @return a list of string representations of the well known text attached
     *  to the given uri
     */
    public List<String> wkt(String uri) {
        return lookup(uri, HAS_GEOMETRY)
                .stream()
                .map(Literal::getString)
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
            .map(Literal::getString)
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
        return links(uri, "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT ?node ?title ?publicationStatus ?type ?rel WHERE {?node ?rel ?me; dct:references ?me; dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; dct:type 'modelApplication'}");
    }

    /**
     * Models linked to this ModelApplication
     * @param uri of modelApplication
     * @return list of Links to models
     */
    public List<Link> models(String uri) {
        return links(uri, "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT ?node ?title ?publicationStatus ?type ?rel WHERE { ?me ?rel ?node; dct:references ?node. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; dct:type 'model'}");
    }

    /**
     * NERC Modeluse linked to this NERC model
     * @param uri of model
     * @return list of Links to modelApplications
     */
    public List<Link> nercModelUses(String uri) {
        return links(uri, "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT ?node ?title ?publicationStatus ?type ?rel WHERE {?node ?rel ?me; dct:references ?me; dct:title ?title; pso:PublicationStatus ?publicationStatus;  dct:type ?type; dct:type 'nercModelUse'}");
    }

    /**
     * NERC Models linked to this NERC Modeluse
     * @param uri of modelUse
     * @return list of Links to models
     */
    public List<Link> nercModels(String uri) {
        return links(uri, "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT ?node ?title ?publicationStatus ?type ?rel WHERE { ?me ?rel ?node; dct:references ?node. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; dct:type 'nercModels'}");
    }

    public List<Link> datasets(String uri) {
        return links(uri, "PREFIX dct: <http://purl.org/dc/terms/>PREFIX pso: <http://purl.org/spar/pso/>SELECT DISTINCT ?node ?title ?publicationStatus ?type ?rel WHERE{{{?me ?rel ?node; dct:references ?node.} UNION {?node ?rel ?me; dct:references ?me.} ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; dct:type 'dataset'.} UNION {?me ?rel ?node; dct:references ?node. ?node dct:source _:n . _:n dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; dct:type 'dataset'.}}");
    }

    public List<Link> relationships(String uri, String relation) {
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> PREFIX eidc: <https://vocabs.ceh.ac.uk/eidc#> PREFIX ef: <http://onto.ceh.ac.uk/EF#> " +
            "SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?geom (IF(BOUND(?geom), true, false) AS ?hasGeom)" +
            "WHERE {?me ?rel ?node; ?relation ?node. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus;  dct:type ?type. " +
            "OPTIONAL {?node ef:hasGeometry ?geom} " +
            "OPTIONAL {?node dct:available ?publicationDate} " +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node ef:hasStatus ?availability}} " +
            "ORDER BY DESC(?publicationDate) ?title";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        pss.setIri("relation", relation);
        return links(pss);
    }

    public List<Link> inverseRelationships(String uri, String relation) {
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> PREFIX eidc: <https://vocabs.ceh.ac.uk/eidc#> PREFIX eidc: <https://vocabs.ceh.ac.uk/eidc#> PREFIX ef: <http://onto.ceh.ac.uk/EF#> " +
            "SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?publicationDate (GROUP_CONCAT(?geo; separator=', ') AS ?geom)" +
            "WHERE {?node ?rel ?me; ?relation ?me. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type. " +
            "OPTIONAL {?node ef:hasGeometry ?geo} " +
            "OPTIONAL {?node dct:available ?publicationDate} " +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node ef:hasStatus ?availability}} " +
            "GROUP BY ?node ?title ?publicationStatus ?availability ?type ?rel ?publicationDate " +
            "ORDER BY DESC(?publicationDate) ?title";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        pss.setIri("relation", relation);
        return links(pss);
    }

    public List<Link> relationshipsWithOwner(String uri, String relation) {
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> PREFIX eidc: <https://vocabs.ceh.ac.uk/eidc#> PREFIX ef: <http://onto.ceh.ac.uk/EF#> " +
            "SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?geom " +
            "WHERE {{?me dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; ef:hasGeometry ?geom. BIND(?me as ?node)} " +
            "UNION {?me ?relation ?node. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; ef:hasGeometry ?geom. BIND(?relation as ?rel)} " +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node ef:hasStatus ?availability}}" +
            "ORDER BY ?title";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        pss.setIri("relation", relation);
        return links(pss);
    }

    public List<Link> programmeFeatures(String uri) {
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> PREFIX ef: <http://onto.ceh.ac.uk/EF#> PREFIX eidc: <https://vocabs.ceh.ac.uk/eidc#> PREFIX ef: <http://onto.ceh.ac.uk/EF#> " +
            "SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?geom " +
            "WHERE {{?me ef:utilises ?node. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus;  dct:type ?type; ef:hasGeometry ?geom} " +
            "UNION {?me ef:utilises ?network. ?node ef:belongsTo ?network; dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type; ef:hasGeometry ?geom. BIND(ef:utilises as ?rel)}" +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node ef:hasStatus ?availability}}";

        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        return links(pss);
    }

    /**
     * Function to compile a FeatureCollection for programmes - directly linked facilities and child facilities of networks
     */
    public String programmeCombinedGeometries(String uri) throws JsonProcessingException {
        List<Link> links = programmeFeatures(uri);
        // Return if no links found
        if (links.isEmpty()) {
            return "";
        }

        List<JsonNode> features = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Link link : links) {
            if (!link.getGeometry().isEmpty()) {
                JsonNode jsonNode = mapper.readTree(link.getGeometry());
                ObjectNode propertiesNode;
                if(jsonNode.has("properties")) {
                    propertiesNode = (ObjectNode)jsonNode.get("properties");
                } else {
                    propertiesNode = mapper.createObjectNode();
                    ((ObjectNode)jsonNode).set("properties", propertiesNode);
                }
                propertiesNode.put("title", link.getTitle());
                propertiesNode.put("link", link.getHref());
                propertiesNode.put("availability", link.getAvailability());
                features.add(jsonNode);
            }
        }

        // Prevent output (and hence plotting map) if no Geometry information found
        if (features.isEmpty()) {
            return "";
        }

        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.set("features", mapper.valueToTree(features));
        return mapper.writeValueAsString(featureCollection);
    }

    /**
     * Function to compile a FeatureCollection from Geometries found in inversely related records
     */
    public String inverseRelationshipCombinedGeometries(String uri, String relation) throws JsonProcessingException {
        List<Link> links = inverseRelationships(uri, relation);
        return getCombinedGeometriesString(links, uri, false);
    }

    public String relationshipCombinedGeometriesWithOwner(String uri, String relation, boolean locationConfidential) throws JsonProcessingException {
        List<Link> links = relationshipsWithOwner(uri, relation);
        return getCombinedGeometriesString(links, uri, locationConfidential);
    }

    private String getCombinedGeometriesString(List<Link> links, String uri, boolean locationConfidential) throws JsonProcessingException {
        // Return if no links found
        if (links.isEmpty()) {
            return "";
        }

        List<JsonNode> features = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Link link : links) {
            if (!link.getGeometry().isEmpty()) {
                JsonNode jsonNode = mapper.readTree(link.getGeometry());
                ObjectNode propertiesNode;
                if(jsonNode.has("properties")) {
                    propertiesNode = (ObjectNode)jsonNode.get("properties");
                } else {
                    propertiesNode = mapper.createObjectNode();
                    ((ObjectNode)jsonNode).set("properties", propertiesNode);
                }
                propertiesNode.put("title", link.getTitle());
                if (link.getHref().equals(uri)) {
                    propertiesNode.put("showPolygon", true);
                } else {
                    propertiesNode.put("link", link.getHref());
                }
                propertiesNode.put("availability", link.getAvailability());
                propertiesNode.put("locationConfidential", locationConfidential);
                features.add(jsonNode);
            }
        }

        // Prevent output (and hence plotting map) if no Geometry information found
        if (features.isEmpty()) {
            return "";
        }

        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.set("features", mapper.valueToTree(features));
        return mapper.writeValueAsString(featureCollection);
    }

    public List<Link> allRelatedRecords(String uri) {
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT ?node ?rel ?title ?publicationStatus ?type WHERE {{?me ?rel ?node. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type.} UNION {?node ?rel ?me. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type.}FILTER(REGEX(STR(?rel),'^https://vocabs.ceh.ac.uk/eidc#'))}";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        return links(pss);
    }

    /**
     * This finds the most recent version of a resource
     * i.e. if a replaced resource is itself replaced, it will return
     * only the last in the chain
     */
    public List<Link> latestVersion(String uri) {
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT DISTINCT ?node ?type ?title ?rel ?publicationStatus WHERE {?node dct:replaces+ ?me; dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type.  BIND( dct:replaces as ?rel)FILTER (!EXISTS {?x dct:replaces ?node})}";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        return links(pss);
    }

    /**
     * This finds resources that the current resource replaces
     * (if the resource is not itself replaced)
     * and orders them by distance to the most recent version
     */
    public List<Link> replaces(String uri) {
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT ?node ?type ?title ?rel ?publicationStatus WHERE {?me (dct:replaces)+ ?mid. ?mid(dct:replaces)* ?node. ?node dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type. BIND(dct:isReplacedBy as ?rel) FILTER (!EXISTS {?x dct:replaces ?me})} GROUP BY ?node ?type ?title ?rel ?publicationStatus HAVING (COUNT(?mid) > 0) ORDER BY COUNT(?mid)";
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        return links(pss);
    }

    public Link metadata(String id) {
        id = nullToEmpty(id);
        String sparql = "PREFIX dct: <http://purl.org/dc/terms/> PREFIX pso: <http://purl.org/spar/pso/> SELECT ?node ?title ?publicationStatus ?type ?rel WHERE {?node dct:identifier ?id; dct:title ?title; pso:PublicationStatus ?publicationStatus; dct:type ?type. BIND(<https://vocabs.ceh.ac.uk/eidc#> as ?rel)}";
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
            qexec.execSelect().forEachRemaining(s -> toReturn.add(
                Link.builder()
                    .title(s.getLiteral("title").getString())
                    .publicationStatus(s.getLiteral("publicationStatus").getString())
                    .availability(s.getLiteral("availability") != null ? s.getLiteral("availability").getString() : "")
                    .href(s.getResource("node").getURI())
                    .associationType(s.getLiteral("type").getString())
                    .rel(s.getResource("rel") != null ? s.getResource("rel").getURI() : "")
                    .geometry(s.getLiteral("geom") != null ? s.getLiteral("geom").getString() : "")
                    .build()
            ));
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
        return getLiterals(pss);
    }

    /**
     * Lookup a resource (the subject) and return a property of that subject.
     * @param objectUri uri of resource (the object)
     * @param relationshipToSubject uri of relationship to subject
     * @param relationshipOnSubject uri of literal on subject
     * @return property of subject
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
        return getLiterals(pss);
    }

    private List<Literal> getLiterals(ParameterizedSparqlString pss) {
        List<Literal> toReturn = new ArrayList<>();
        jenaTdb.begin(ReadWrite.READ);
        try (QueryExecution q = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            q.execSelect().forEachRemaining(s -> toReturn.add(s.getLiteral("attr")));
        } finally {
            jenaTdb.end();
        }
        return toReturn;
    }

    /**
     * Return all the EIDC incoming relations for a collection
     *
     * @return List of relations
     */
    public List<Link> incomingEidcRelations(String uri) {
        val query = "SELECT * " +
                    "WHERE { " +
                    "  ?node ?rel ?me ; " +
                    "  <http://purl.org/dc/terms/title> ?title ; " +
                    "  <http://purl.org/spar/pso/PublicationStatus> ?publicationStatus ; " +
                    "  <http://purl.org/dc/terms/type>  ?type . " +
                    "FILTER(strstarts(str(?rel), 'https://vocabs.ceh.ac.uk/eidc#'))" +
                    "}";
        return links(uri, query);
    }
}
