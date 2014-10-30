package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 * The following class is responsible for taking a gemini document and creating 
 * beans which are solr indexable
 * @author cjohn
 */
public class MetadataDocumentSolrIndexGenerator implements SolrIndexGenerator<MetadataDocument> {
    private final TopicIndexer topicIndexer;

    public MetadataDocumentSolrIndexGenerator(TopicIndexer topicIndexer) {
        this.topicIndexer = topicIndexer;
    }

    @Override
    public DocumentSolrIndex generateIndex(MetadataDocument document) {
        return new DocumentSolrIndex()
                .setDescription(document.getDescription())
                .setTitle(document.getTitle())
                .setIdentifier(document.getId())
                .setResourceType(document.getType())
                .setLocations(document.getLocations())
                .setIsOgl(getIsOgl(document))
                .setState(getState(document))
                .setTopic(topicIndexer.index(document));
    }
    
    private Boolean getIsOgl(MetadataDocument document){
        if(document instanceof GeminiDocument) {
            GeminiDocument geminiDocument = (GeminiDocument)document;
            if(geminiDocument.getDownloadOrder() != null){
                return geminiDocument.getDownloadOrder().isOgl();
            }
        }
        return null;
    }

    private String getState(MetadataDocument document) {
        if (document.getMetadata() != null) {
            return document.getMetadata().getState();
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
    public static class DocumentSolrIndex {
        protected static final int MAX_DESCRIPTION_CHARACTER_LENGTH = 265;
        private @Field String identifier;
        private @Field String title;
        private @Field String description;
        private @Field String resourceType;
        private @Field List<String> locations;
        private @Field Boolean isOgl;
        private @Field String state;
        private @Field List<String> topic;
        
        public String getShortenedDescription(){
            return shortenLongString(description, MAX_DESCRIPTION_CHARACTER_LENGTH);
        }
        
        private String shortenLongString(String toShorten, int desiredLength){
            if(toShorten.length() > desiredLength){
                return breakAtNextSpace(toShorten);
            }else{
                return toShorten;
            }
        }
        
        private String breakAtNextSpace(String toBreak){
            int nextSpace = toBreak.indexOf(" ", MAX_DESCRIPTION_CHARACTER_LENGTH);
            String toReturn;
            if(nextSpace != -1){
                toReturn = toBreak.substring(0,nextSpace);
            }else{
                toReturn = toBreak.substring(0,MAX_DESCRIPTION_CHARACTER_LENGTH);
            }
            return toReturn + "...";
        }
    }
}