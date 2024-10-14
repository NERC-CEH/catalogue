package uk.ac.ceh.gateway.catalogue.indexing.jena;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Ontology {
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
    public static final Property RESOURCE_STATUS = ResourceFactory.createProperty("https://vocabs.ceh.ac.uk/eidc#resourceStatus");

    public static final Property ANYREL = ResourceFactory.createProperty("https://vocabs.ceh.ac.uk/eidc#");
    public static final Property EIDC_MEMBER_OF = ResourceFactory.createProperty("https://vocabs.ceh.ac.uk/eidc#memberOf");
    public static final Property EIDC_USES = ResourceFactory.createProperty("https://vocabs.ceh.ac.uk/eidc#uses");

    public static final Property SET_UP_FOR = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#setUpFor");
    public static final Property USES = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#uses");
    public static final Property INVOLVED_IN = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#involvedIn");
    public static final Property SUPERSEDES = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#supersedes");
    public static final Property SUPERSEDED_BY = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#supersededBy");
    public static final Property BROADER = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#broader");
    public static final Property NARROWER = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#narrower");
    public static final Property BELONGS_TO = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#belongsTo");
    public static final Property RELATED_TO = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#relatedTo");
    public static final Property UTILISES = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#utilises");
    public static final Property ASSOCIATED_WITH = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#associatedWith");
    public static final Property CONTAINS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#contains");
    public static final Property TRIGGERS = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#triggers");

    public static final Property LINKING_TIME = ResourceFactory.createProperty("http://onto.ceh.ac.uk/EF#linkingTime");
    public static final Property TEMPORAL_BEGIN = ResourceFactory.createProperty("http://def.seegrid.csiro.au/isotc211/iso19108/2002/temporal#begin");
    public static final Property TEMPORAL_END = ResourceFactory.createProperty("http://def.seegrid.csiro.au/isotc211/iso19108/2002/temporal#end");

    public static final Property HAS_GEOMETRY = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#hasGeometry");
    public static final RDFDatatype WKT_LITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#wktLiteral");
    public static final RDFDatatype GEOJSON_LITERAL = new BaseDatatype("http://www.opengis.net/ont/geosparql#geoJSONLiteral");
}
