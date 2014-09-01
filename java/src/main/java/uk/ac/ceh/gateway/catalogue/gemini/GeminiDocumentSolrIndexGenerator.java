package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.List;
import java.util.Map;
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
    private final ScienceAreaIndexer scienceAreaIndexer;

    public GeminiDocumentSolrIndexGenerator(ScienceAreaIndexer scienceAreaIndexer) {
        this.scienceAreaIndexer = scienceAreaIndexer;
    }

    @Override
    public GeminiDocumentSolrIndex generateIndex(GeminiDocument document) {
        Map<String, String> sci = scienceAreaIndexer.index(document);
        return new GeminiDocumentSolrIndex()
                .setDescription(document.getDescription())
                .setTitle(document.getTitle())
                .setIdentifier(document.getId())
                .setResourceType(getResourceType(document))
                .setIsOgl(getIsOgl(document))
                .setState(getState(document))
                .setSci0(sci.get("sci0"))
                .setSci1(sci.get("sci1"));
    }
    
    private String getResourceType(GeminiDocument document){
        if(document.getResourceType() != null){
            return document.getResourceType().getValue();
        } else {
            return null;
        }
    }
    
    private Boolean getIsOgl(GeminiDocument document){
        if(document.getDownloadOrder() != null){
            return document.getDownloadOrder().isOgl();
        } else {
            return null;
        }
    }

    private String getState(GeminiDocument document) {
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
    public static class GeminiDocumentSolrIndex {
        protected static final int MAX_DESCRIPTION_CHARACTER_LENGTH = 530;
        private @Field String identifier;
        private @Field String title;
        private @Field String description;
        private @Field String resourceType;
        private @Field Boolean isOgl;
        private @Field String state;
        private @Field String sci0;
        private @Field String sci1;
        
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
            String toReturn = "";
            if(nextSpace != -1){
                toReturn = toBreak.substring(0,nextSpace);
            }else{
                toReturn = toBreak.substring(0,MAX_DESCRIPTION_CHARACTER_LENGTH);
            }
            return toReturn + "...";
        }
    }
}