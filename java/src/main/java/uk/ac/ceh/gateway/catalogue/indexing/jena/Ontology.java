package uk.ac.ceh.gateway.catalogue.indexing.jena;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Ontology {
    // Classes
    public static final Property CLASS_DCATDATASET = ResourceFactory.createProperty("http://www.w3.org/ns/dcat#Dataset");
    public static final Property CLASS_EMF = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#Facility");
    public static final Property CLASS_EMN = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#Network");
    public static final Property CLASS_EMP = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#Programme");

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

    public static final Property RESOURCE_STATUS = ResourceFactory.createProperty("https://vocabs.ceh.ac.uk/eidc#availability");

    public static final Property SET_UP_FOR = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#setUpFor");
    public static final Property USES = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#uses");
    public static final Property INVOLVED_IN = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#involvedIn");
    //public static final Property BROADER = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#broader");
    //public static final Property NARROWER = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#narrower");
    //public static final Property HASCHILD = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#hasChild");
    public static final Property UTILISES = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#utilises");
    public static final Property CONTAINS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#contains");
    public static final Property TRIGGERS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#triggers");
    public static final Property HAS_STATUS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#hasStatus");

    // Datatypes
    public static final RDFDatatype GEO_WKTLITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#wktLiteral");
    public static final RDFDatatype GEO_GEOJSONLITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#geoJSONLiteral");

}
