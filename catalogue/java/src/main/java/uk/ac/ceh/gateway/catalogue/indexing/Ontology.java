package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 *
 * @author cjohn
 */
public class Ontology {
    public static final Property IDENTIFIER = ResourceFactory.createProperty("http://purl.org/dc/terms/identifier");
    public static final Property TITLE      = ResourceFactory.createProperty("http://purl.org/dc/terms/title");
    public static final Property TYPE       = ResourceFactory.createProperty("http://purl.org/dc/terms/type");
    public static final Property IS_PART_OF = ResourceFactory.createProperty("http://purl.org/dc/terms/isPartOf");
    
    public static final Property SET_UP_FOR = ResourceFactory.createProperty("http://purl.org/voc/ef#setUpFor");
    public static final Property USES       = ResourceFactory.createProperty("http://purl.org/voc/ef#uses");
    public static final Property SUPERSEDES = ResourceFactory.createProperty("http://purl.org/voc/ef#supersedes");
    public static final Property BROADER    = ResourceFactory.createProperty("http://purl.org/voc/ef#broader");
    public static final Property BELONGS_TO = ResourceFactory.createProperty("http://purl.org/voc/ef#belongsTo");
    public static final Property RELATED_TO = ResourceFactory.createProperty("http://purl.org/voc/ef#relatedTo");
}
