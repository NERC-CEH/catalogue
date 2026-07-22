package uk.ac.ceh.gateway.catalogue.templateHelpers;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * A simple lookup service powered by the jena linking database. This just looks
 * up any literals associated to a given uri
 *
 * <p>The SPARQL query text used by each method is static: only the record URI (and, for the
 * relationship queries, the relation predicate) varies between calls. Parsing SPARQL is expensive —
 * profiling showed the ARQ parser dominating individual-record render CPU because
 * {@code ParameterizedSparqlString.asQuery()} re-parsed the same query text on every call. We now
 * parse each query string once ({@link #parse}) and inject the per-call values as an execution-time
 * {@link QuerySolutionMap} substitution, which rewrites the query the same way the old
 * {@code setIri}/{@code setLiteral} text-substitution did — but without re-parsing.</p>
 */
@SuppressWarnings({"unused"})
@Slf4j
@ToString
@Service
public class JenaLookupService {
    private final Dataset jenaTdb;

    private static final String PREFIXES =
        "PREFIX doo: <https://digital.ceh.ac.uk/ontology/doo/> " +
        "PREFIX dcterms: <http://purl.org/dc/terms/> " +
        "PREFIX pso: <http://purl.org/spar/pso/> " +
        "PREFIX eidc: <https://vocabs.ceh.ac.uk/eidc#> " +
        "PREFIX sf: <http://www.opengis.net/ont/sf#> ";

    /**
     * Parsed-query cache keyed by the (static) query text. A parsed {@link Query} is immutable and
     * safe to share across request threads; only the cheap per-call substitution + execution remains.
     */
    private static final Map<String, Query> QUERY_CACHE = new ConcurrentHashMap<>();

    private static Query parse(String sparql) {
        return QUERY_CACHE.computeIfAbsent(sparql, QueryFactory::create);
    }

    private static QuerySolutionMap me(String uri) {
        val binding = new QuerySolutionMap();
        binding.add("me", createResource(uri));
        return binding;
    }

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
        return lookup(uri, SF_GEOMETRY)
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
        return lookupPropertyOfSubject(uri, DCTERMS_SOURCE, DCTERMS_IDENTIFIER)
            .stream()
            .map(Literal::getString)
            .filter(l -> !l.startsWith("CEH:EIDC:"))
            .filter(l -> !l.startsWith("doi"))
            .filter(l -> !l.startsWith("http"))
            .collect(Collectors.toList());
    }

    /**
     * ModelApplications linked to this Model
     * @param uri of model
     * @return list of Links to modelApplications
     */
    public List<Link> modelApplications(String uri) {
        return links(uri, PREFIXES + " SELECT ?node ?title ?publicationStatus ?type ?rel WHERE {?node ?rel ?me; dcterms:references ?me; dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; dcterms:type 'modelApplication'}");
    }

    /**
     * Models linked to this ModelApplication
     * @param uri of modelApplication
     * @return list of Links to models
     */
    public List<Link> models(String uri) {
        return links(uri, PREFIXES + " SELECT ?node ?title ?publicationStatus ?type ?rel WHERE { ?me ?rel ?node; dcterms:references ?node. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; dcterms:type 'model'}");
    }

    /**
     * NERC Modeluse linked to this NERC model
     * @param uri of model
     * @return list of Links to modelApplications
     */
    public List<Link> nercModelUses(String uri) {
        return links(uri, PREFIXES + " SELECT ?node ?title ?publicationStatus ?type ?rel WHERE {?node ?rel ?me; dcterms:references ?me; dcterms:title ?title; pso:PublicationStatus ?publicationStatus;  dcterms:type ?type; dcterms:type 'nercModelUse'}");
    }

    /**
     * NERC Models linked to this NERC Modeluse
     * @param uri of modelUse
     * @return list of Links to models
     */
    public List<Link> nercModels(String uri) {
        return links(uri, PREFIXES + " SELECT ?node ?title ?publicationStatus ?type ?rel WHERE { ?me ?rel ?node; dcterms:references ?node. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; dcterms:type 'nercModels'}");
    }

    public List<Link> datasets(String uri) {
        return links(uri, PREFIXES + " SELECT DISTINCT ?node ?title ?publicationStatus ?type ?rel WHERE{{{?me ?rel ?node; dcterms:references ?node.} UNION {?node ?rel ?me; dcterms:references ?me.} ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; dcterms:type 'dataset'.} UNION {?me ?rel ?node; dcterms:references ?node. ?node dcterms:source _:n . _:n dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; dcterms:type 'dataset'.}}");
    }

    public List<Link> relationships(String uri, String relation) {
        String sparql =  PREFIXES + " SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?geom (IF(BOUND(?geom), true, false) AS ?hasGeom) (GROUP_CONCAT(?code; separator='|') AS ?codes) " +
            "WHERE {?me ?rel ?node; ?relation ?node. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus;  dcterms:type ?type. " +
            "OPTIONAL {?node <http://www.opengis.net/ont/sf#Geometry> ?geom} " +
            "OPTIONAL {?node dcterms:available ?publicationDate} " +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node doo:operationalStatus ?availability} " +
            "OPTIONAL {?node dcterms:identifier ?code}} " +
            "GROUP BY ?node ?title ?publicationStatus ?availability ?type ?rel ?geom ?hasGeom ?publicationDate " +
            "ORDER BY DESC(?publicationDate) ?title";
        val binding = me(uri);
        binding.add("relation", createResource(relation));
        return links(sparql, binding);
    }

    public List<Link> inverseRelationships(String uri, String relation) {
        String sparql = PREFIXES + " SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?publicationDate (GROUP_CONCAT(?geo; separator=', ') AS ?geom) (GROUP_CONCAT(?code; separator='|') AS ?codes) " +
            "WHERE {?node ?rel ?me; ?relation ?me. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type. " +
            "OPTIONAL {?node <http://www.opengis.net/ont/sf#Geometry> ?geo} " +
            "OPTIONAL {?node dcterms:available ?publicationDate} " +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node doo:operationalStatus ?availability} " +
            "OPTIONAL {?node dcterms:identifier ?code}} " +
            "GROUP BY ?node ?title ?publicationStatus ?availability ?type ?rel ?publicationDate ?geom " +
            "ORDER BY DESC(?publicationDate) ?title";
        val binding = me(uri);
        binding.add("relation", createResource(relation));
        return links(sparql, binding);
    }

    public List<Link> relationshipsWithOwner(String uri, String relation) {
        String sparql = PREFIXES + " SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?geom (GROUP_CONCAT(?code; separator='|') AS ?codes) " +
            "WHERE {{?me dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; sf:Geometry ?geom. BIND(?me as ?node)} " +
            "UNION {?me ?relation ?node. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; sf:Geometry ?geom. BIND(?relation as ?rel)} " +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node doo:operationalStatus ?availability} " +
            "OPTIONAL {?node dcterms:identifier ?code}} " +
            "GROUP BY ?node ?title ?publicationStatus ?availability ?type ?rel ?geom " +
            "ORDER BY ?title";
        val binding = me(uri);
        binding.add("relation", createResource(relation));
        return links(sparql, binding);
    }

    public List<Link> programmeFeatures(String uri) {
        String sparql = PREFIXES + " SELECT DISTINCT ?node ?title ?publicationStatus ?availability ?type ?rel ?geom (GROUP_CONCAT(?code; separator='|') AS ?codes) " +
            "WHERE {{?me doo:utilises ?node. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus;  dcterms:type ?type;sf:Geometry ?geom} " +
            "UNION {?me doo:utilises ?network. ?node dcterms:isPartOf ?network; dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type; sf:Geometry ?geom. BIND(doo:utilises as ?rel)}" +
            "OPTIONAL {?node eidc:availability ?availability} " +
            "OPTIONAL {?node doo:operationalStatus ?availability} " +
            "OPTIONAL {?node dcterms:identifier ?code}} " +
            "GROUP BY ?node ?title ?publicationStatus ?availability ?type ?rel ?geom";
        return links(sparql, me(uri));
    }

    /**
     * Function to compile a FeatureCollection for programmes - directly linked facilities and child facilities of networks
     */
    public String programmeCombinedGeometries(String uri) throws JacksonException {
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
    public String inverseRelationshipCombinedGeometries(String uri, String relation) throws JacksonException {
        List<Link> links = inverseRelationships(uri, relation);
        return getCombinedGeometriesString(links, uri, false);
    }

    public String relationshipCombinedGeometriesWithOwner(String uri, String relation, boolean locationConfidential) throws JacksonException {
        List<Link> links = relationshipsWithOwner(uri, relation);
        return getCombinedGeometriesString(links, uri, locationConfidential);
    }

    private String getCombinedGeometriesString(List<Link> links, String uri, boolean locationConfidential) throws JacksonException {
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
        String sparql =  PREFIXES + " SELECT ?node ?rel ?title ?publicationStatus ?type WHERE {{?me ?rel ?node. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type.} UNION {?node ?rel ?me. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type.} FILTER (?rel IN (dcterms:references, dcterms:replaces, dcterms:requires, dcterms:isPartOf, dcterms:relation))}";
        return links(sparql, me(uri));
    }

    /**
     * This finds the most recent version of a resource
     * i.e. if a replaced resource is itself replaced, it will return
     * only the last in the chain
     */
    public List<Link> latestVersion(String uri) {
        String sparql =  PREFIXES + " SELECT DISTINCT ?node ?type ?title ?rel ?publicationStatus WHERE {?node dcterms:replaces+ ?me; dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type.  BIND( dcterms:replaces as ?rel)FILTER (!EXISTS {?x dcterms:replaces ?node})}";
        return links(sparql, me(uri));
    }

    /**
     * This finds resources that the current resource replaces
     * (if the resource is not itself replaced)
     * and orders them by distance to the most recent version
     */
    public List<Link> replaces(String uri) {
        String sparql =  PREFIXES + " SELECT ?node ?type ?title ?rel ?publicationStatus WHERE {?me (dcterms:replaces)+ ?mid. ?mid(dcterms:replaces)* ?node. ?node dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type. BIND(dcterms:isReplacedBy as ?rel) FILTER (!EXISTS {?x dcterms:replaces ?me})} GROUP BY ?node ?type ?title ?rel ?publicationStatus HAVING (COUNT(?mid) > 0) ORDER BY COUNT(?mid)";
        return links(sparql, me(uri));
    }

    public Link metadata(String id) {
        id = nullToEmpty(id);
        String sparql =  PREFIXES + " SELECT ?node ?title ?publicationStatus ?type ?rel WHERE {?node dcterms:identifier ?id; dcterms:title ?title; pso:PublicationStatus ?publicationStatus; dcterms:type ?type. BIND(<https://vocabs.ceh.ac.uk/eidc#> as ?rel)}";
        val binding = new QuerySolutionMap();
        binding.add("id", createPlainLiteral(id));
        return links(sparql, binding).stream().findFirst().orElse(null);
    }

    private List<Link> links(@NonNull String uri, String sparql) {
        return links(sparql, me(uri));
    }

    private List<Link> links(String sparql, QuerySolutionMap binding) {
        List<Link> toReturn = new ArrayList<>();
        jenaTdb.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecution.create()
                .query(parse(sparql))
                .dataset(jenaTdb)
                .substitution(binding)
                .build()) {
            qexec.execSelect().forEachRemaining(s -> {
                List<ResourceIdentifier> resourceIdentifiers = new ArrayList<>();
                if (s.getLiteral("codes") != null) {
                    String[] coupledResources = s.getLiteral("codes").getString().split("\\|");

                    for (String coupledResource : coupledResources) {
                        if (!coupledResource.isEmpty()) {
                            if (coupledResource.contains("#")) {
                                String[] parts = coupledResource.split("#", 2);
                                resourceIdentifiers.add(ResourceIdentifier.builder()
                                    .code(parts[1])
                                    .codeSpace(parts[0])
                                    .build());
                            }
                        }
                    }
                }
                toReturn.add(
                    Link.builder()
                        .title(s.getLiteral("title").getString())
                        .publicationStatus(s.getLiteral("publicationStatus").getString())
                        .availability(s.getLiteral("availability") != null ? s.getLiteral("availability").getString() : "")
                        .href(s.getResource("node").getURI())
                        .associationType(s.getLiteral("type").getString())
                        .rel(s.getResource("rel") != null ? s.getResource("rel").getURI() : "")
                        .geometry(s.getLiteral("geom") != null ? s.getLiteral("geom").getString() : "")
                        .resourceIdentifiers(resourceIdentifiers)
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
        val binding = new QuerySolutionMap();
        binding.add("uri", createResource(uri));
        binding.add("relationship", relationship);
        return getLiterals("SELECT ?attr WHERE { ?uri ?relationship ?attr }", binding);
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
        val binding = new QuerySolutionMap();
        binding.add("objectUri", createResource(objectUri));
        binding.add("relationshipToSubject", relationshipToSubject);
        binding.add("relationshipOnSubject", relationshipOnSubject);
        return getLiterals(
            "SELECT ?attr WHERE { _:s ?relationshipToSubject ?objectUri ; ?relationshipOnSubject ?attr . }",
            binding
        );
    }

    private List<Literal> getLiterals(String sparql, QuerySolutionMap binding) {
        List<Literal> toReturn = new ArrayList<>();
        jenaTdb.begin(ReadWrite.READ);
        try (QueryExecution q = QueryExecution.create()
                .query(parse(sparql))
                .dataset(jenaTdb)
                .substitution(binding)
                .build()) {
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
        val query =  PREFIXES + " SELECT * WHERE {?node dcterms:isPartOf ?me ; dcterms:title ?title; <http://purl.org/spar/pso/PublicationStatus> ?publicationStatus; dcterms:type ?type .}";
        return links(uri, query);
    }
    //WRONG
}
