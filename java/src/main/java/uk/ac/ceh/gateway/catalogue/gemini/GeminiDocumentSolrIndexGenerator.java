package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGenerator;

/**
 * The following class is responsible for taking a gemini document and creating 
 * beans which are solr indexable
 * @author cjohn
 */
public class GeminiDocumentSolrIndexGenerator implements SolrIndexGenerator<GeminiDocument> {

    @Override
    public GeminiDocumentSolrIndex generateIndex(GeminiDocument document) {
        return new GeminiDocumentSolrIndex()
                .setDescription(document.getDescription())
                .setTitle(document.getTitle())
                .setIdentifier(document.getId())
                .setResourceType(getResourceType(document))
                .setIsOgl(getIsOgl(document));
    }
    
    private final String getResourceType(GeminiDocument document){
        if(document.getResourceType() != null){
            return document.getResourceType().getValue();
        } else {
            return null;
        }
    }
    
    private final Boolean getIsOgl(GeminiDocument document){
        if(document.getDownloadOrder() != null){
            return document.getDownloadOrder().isOgl();
        } else {
            return null;
        }
    }
    
    /**
    * The following represents the elements of a gemini document which can be indexed
    * by solr
    * @author cjohn
    */
    @Data
    @Accessors(chain=true)
    public static class GeminiDocumentSolrIndex {
        private @Field String identifier;
        private @Field String title;
        private @Field String description;
        private @Field String resourceType;
        private @Field Boolean isOgl;
    }
}