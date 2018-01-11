package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author cjohn
 */
/**
* The following represents the elements of a gemini document which can be indexed
* by solr
* @author cjohn
*/
@Data
@Accessors(chain=true)
public class SolrIndex {
    protected static final int MAX_DESCRIPTION_CHARACTER_LENGTH = 265;
    private @Field String identifier;
    private @Field String title;
    private @Field List<String> altTitle;
    private @Field String description;
    private @Field String lineage;
    private @Field String resourceStatus;
    private @Field List<String> organisation;
    private @Field List<String> individual;
    private @Field List<String> onlineResourceName;
    private @Field List<String> onlineResourceDescription;
    private @Field List<String> resourceIdentifier;
    private @Field String resourceType;
    private @Field List<String> locations;
    private @Field String licence;
    private @Field String state;
    private @Field List<String> topic;
    private @Field List<String> keyword;
    private @Field List<String> view;
    private @Field String catalogue;
    private @Field String documentType;
    private @Field List<String> impCaMMPIssues;
    private @Field List<String> impDataType;
    private @Field List<String> impScale;
    private @Field List<String> impTopic;
    private @Field List<String> impWaterPollutant;
    private @Field List<String> modelType;
    private @Field List<String> inmsDemonstrationRegion;
    private @Field String manufacturer;
    private @Field String manufacturerName;

    public SolrIndex addLocations(List<String> locations) {
        if(this.locations == null) {
            this.locations = new ArrayList<>();
        }
        this.locations.addAll(locations);
        return this;
    }
    public String getShortenedDescription(){
        return shortenLongString(description, MAX_DESCRIPTION_CHARACTER_LENGTH);
    }

    private String shortenLongString(String toShorten, int desiredLength){
        toShorten = Strings.nullToEmpty(toShorten);
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