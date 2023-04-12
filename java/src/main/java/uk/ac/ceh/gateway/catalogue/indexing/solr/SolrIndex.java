package uk.ac.ceh.gateway.catalogue.indexing.solr;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

@Data
@Accessors(chain=true)
public class SolrIndex {
    protected static final int MAX_DESCRIPTION_CHARACTER_LENGTH = 265;
    private @Field List<String> altTitle;
    private @Field List<String> assistResearchThemes;
    private @Field List<String> assistTopics;
    private @Field List<String> authorAffiliation;
    private @Field List<String> authorName;
    private @Field List<String> authorOrcid;
    private @Field List<String> authorRor;
    private @Field String catalogue;
    private @Field String condition;
    private @Field String description;
    private @Field String documentType;
    private @Field List<String> elterDeimsSite;
    private @Field List<String> elterDeimsUri;
    private @Field String importId;
    private @Field List<String> funder;
    private @Field List<String> grant;
    private @Field String identifier;
    private @Field List<String> impCaMMPIssues;
    private @Field List<String> impDataType;
    private @Field List<String> impScale;
    private @Field List<String> impTopic;
    private @Field List<String> impWaterPollutant;
    private @Field long incomingCitationCount;
    private @Field List<String> individual;
    private @Field List<String> inmsDemonstrationRegion;
    private @Field List<String> inmsProject;
    private @Field List<String> keyword;
    private @Field String licence;
    private @Field String lineage;
    private @Field String dataLevel;
    private @Field List<String> locations;
    private @Field List<String> modelType;
    private @Field List<String> ncAssets;
    private @Field List<String> ncCaseStudy;
    private @Field List<String> ncDrivers;
    private @Field List<String> ncEcosystemServices;
    private @Field List<String> ncGeographicalScale;
    private @Field List<String> orcid;
    private @Field List<String> organisation;
    private @Field String recordType;
    private @Field List<String> resourceIdentifier;
    private @Field String resourceStatus;
    private @Field String resourceType;
    private @Field List<String> rightsHolder;
    private @Field List<String> ror;
    private @Field List<String> saPhysicalState;
    private @Field List<String> saSpecimenType;
    private @Field List<String> saTaxon;
    private @Field List<String> saTissue;
    private @Field String state;
    private @Field List<String> supplementalDescription;
    private @Field List<String> supplementalName;
    private @Field String title;
    private @Field List<String> topic;
    private @Field List<String> ukscapeResearchProject;
    private @Field List<String> ukscapeResearchTheme;
    private @Field List<String> ukscapeScienceChallenge;
    private @Field List<String> ukscapeService;
    private @Field Number version;
    private @Field List<String> view;
    // infrastructure catalogue
    private @Field String scienceArea;
    private @Field String infrastructureCapabilities;
    private @Field String infrastructureScale;
    private @Field List<String> infrastructureChallenge;
    private @Field List<String> infrastructureCategory;
    private @Field List<String> infrastructureClass;


    public SolrIndex addLocations(List<String> locations) {
        this.locations.addAll(locations);
        return this;
    }

    public SolrIndex addLocation(String location) {
        this.locations.add(location);
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
