package uk.ac.ceh.gateway.catalogue.indexing.jena;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Ontology {
    // Classes
    public static final Property CLASS_DCATDATASET = ResourceFactory.createProperty("http://www.w3.org/ns/dcat#Dataset");

    // Properties
    public static final Property DCTERMS_IDENTIFIER = ResourceFactory.createProperty("http://purl.org/dc/terms/identifier");
    public static final Property DCTERMS_TITLE = ResourceFactory.createProperty("http://purl.org/dc/terms/title");
    public static final Property DCTERMS_TYPE = ResourceFactory.createProperty("http://purl.org/dc/terms/type");
    public static final Property DCTERMS_STATUS = ResourceFactory.createProperty("http://purl.org/dc/terms/status");
    public static final Property DCTERMS_ISPARTOF = ResourceFactory.createProperty("http://purl.org/dc/terms/isPartOf");
    public static final Property DCTERMS_REPLACES = ResourceFactory.createProperty("http://purl.org/dc/terms/replaces");
    public static final Property DCTERMS_REFERENCES = ResourceFactory.createProperty("http://purl.org/dc/terms/references");
    public static final Property DCTERMS_SOURCE = ResourceFactory.createProperty("http://purl.org/dc/terms/source");
    public static final Property DCTERMS_AVAILABLE = ResourceFactory.createProperty("http://purl.org/dc/terms/available");
    public static final Property DCTERMS_REQUIRES = ResourceFactory.createProperty("http://purl.org/dc/terms/requires");
    public static final Property DCTERMS_RELATED = ResourceFactory.createProperty("http://purl.org/dc/terms/related");
    public static final Property PSO_METADATASTATUS = ResourceFactory.createProperty("http://purl.org/spar/pso/PublicationStatus");
    public static final Property SCHEMA_VARIABLEMEASURED = ResourceFactory.createProperty("http://schema.org/variableMeasured");
    public static final Property QUDT_APPLICABLEUNIT = ResourceFactory.createProperty("http://qudt.org/schema/qudt/applicableUnit");
    public static final Property RDFS_LABEL = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
    public static final Property RDF_TYPE = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    public static final Property GEO_FEATURE = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#Feature");
    public static final Property GEO_HASGEOMETRY = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#hasGeometry");
    public static final Property GEO_GEOMETRY = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#Geometry");
    public static final Property GEO_ASWKT = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asWKT");
    public static final Property GEO_ASGEOJSON = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asGeoJSON");
    public static final Property SF_GEOMETRY = ResourceFactory.createProperty("http://www.opengis.net/ont/sf#Geometry");
    public static final Property DOO_USES = ResourceFactory.createProperty("https://digital.ceh.ac.uk/ontology/doo/uses");
    public static final Property DOO_UTILISES = ResourceFactory.createProperty("https://digital.ceh.ac.uk/ontology/doo/utilises");
    public static final Property DOO_TRIGGERS = ResourceFactory.createProperty("https://digital.ceh.ac.uk/ontology/doo/triggers");

    public static final Property RESOURCE_STATUS = ResourceFactory.createProperty("https://vocabs.ceh.ac.uk/eidc#availability");
    public static final Property HAS_STATUS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#hasStatus");

    // Datatypes
    public static final RDFDatatype GEO_WKTLITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#wktLiteral");
    public static final RDFDatatype GEO_GEOJSONLITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#geoJSONLiteral");
}
