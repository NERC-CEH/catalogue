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
    public static final Property IDENTIFIER = ResourceFactory.createProperty("http://purl.org/dc/terms/identifier");
    public static final Property TITLE = ResourceFactory.createProperty("http://purl.org/dc/terms/title");
    public static final Property TYPE = ResourceFactory.createProperty("http://purl.org/dc/terms/type");
    public static final Property STATUS = ResourceFactory.createProperty("http://purl.org/dc/terms/status");
    public static final Property IS_PART_OF = ResourceFactory.createProperty("http://purl.org/dc/terms/isPartOf");
    public static final Property REPLACES = ResourceFactory.createProperty("http://purl.org/dc/terms/replaces");
    public static final Property REFERENCES = ResourceFactory.createProperty("http://purl.org/dc/terms/references");
    public static final Property SOURCE = ResourceFactory.createProperty("http://purl.org/dc/terms/source");
    public static final Property PUBLICATION_DATE = ResourceFactory.createProperty("http://purl.org/dc/terms/available");
    public static final Property METADATA_STATUS = ResourceFactory.createProperty("http://purl.org/spar/pso/PublicationStatus");
    public static final Property RESOURCE_STATUS = ResourceFactory.createProperty("https://vocabs.ceh.ac.uk/eidc#availability");
    public static final Property REQUIRES = ResourceFactory.createProperty("http://purl.org/dc/terms/requires");

    public static final Property SET_UP_FOR = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#setUpFor");
    public static final Property USES = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#uses");
    public static final Property INVOLVED_IN = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#involvedIn");
    public static final Property BROADER = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#broader");
    public static final Property NARROWER = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#narrower");
    public static final Property HASCHILD = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#hasChild");
    public static final Property BELONGS_TO = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#belongsTo");
    public static final Property RELATED_TO = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#relatedTo");
    public static final Property UTILISES = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#utilises");
    public static final Property ASSOCIATED_WITH = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#associatedWith");
    public static final Property CONTAINS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#contains");
    public static final Property TRIGGERS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#triggers");
    public static final Property HAS_STATUS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#hasStatus");
    public static final Property HAS_GEOMETRY = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#hasGeometry");
    public static final Property HAS_OBSERVED_PROPERTY = ResourceFactory.createProperty("http://schema.org/variableMeasured");
    public static final Property HAS_UNIT = ResourceFactory.createProperty("http://qudt.org/schema/qudt/applicableUnit");
    public static final Property RDFS_LABEL = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
    public static final Property RDF_TYPE = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    public static final Property GEO_FEATURE = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#Feature");
    public static final Property GEO_HASGEOMETRY = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#hasGeometry");
    public static final Property GEO_GEOMETRY = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#Geometry");
    public static final Property GEO_ASWKT = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asWKT");
    public static final Property GEO_ASGEOJSON = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#asGeoJSON");

    // Datatypes
    public static final RDFDatatype WKT_LITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#wktLiteral");
    public static final RDFDatatype GEOJSON_LITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#geoJSONLiteral");

}
