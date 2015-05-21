package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ceh.gateway.catalogue.ef.EFDocument;
import uk.ac.ceh.gateway.catalogue.ef.Geometry;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * The following class is responsible for taking a gemini document and creating 
 * beans which are solr indexable
 * @author cjohn
 */
public class MetadataDocumentSolrIndexGenerator implements SolrIndexGenerator<MetadataDocument> {
    private static final String OGL_URL = "http://www.nationalarchives.gov.uk/doc/open-government-licence";
    private static final String CEH_OGL_URL = "http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence";
    private final TopicIndexer topicIndexer;
    private final CodeLookupService codeLookupService;
    private final SolrGeometryService geometryService;

    public MetadataDocumentSolrIndexGenerator(TopicIndexer topicIndexer, CodeLookupService codeLookupService, SolrGeometryService geometryService) {
        this.topicIndexer = topicIndexer;
        this.codeLookupService = codeLookupService;
        this.geometryService = geometryService;
    }

    @Override
    public DocumentSolrIndex generateIndex(MetadataDocument document) {
        DocumentSolrIndex toReturn = new DocumentSolrIndex()
                .setDescription(document.getDescription())
                .setTitle(document.getTitle())
                .setIdentifier(document.getId())
                .setResourceType(codeLookupService.lookup("metadata.resourceType", document.getType()))
                .setState(getState(document))
                .setTopic(topicIndexer.index(document))
                .setView(getViews(document));
        
        if(document instanceof GeminiDocument) {
            GeminiDocument gemini = (GeminiDocument)document;

            toReturn.setAltTitle(gemini.getAlternateTitles())
                    .setLineage(gemini.getLineage())
                    .setLicence(getLicence(gemini))
                    .setOrganisation(grab(gemini.getResponsibleParties(), ResponsibleParty::getOrganisationName))
                    .setIndividual(grab(gemini.getResponsibleParties(), ResponsibleParty::getIndividualName))
                    .setOnlineResourceName(grab(gemini.getOnlineResources(), OnlineResource::getName))
                    .setOnlineResourceDescription(grab(gemini.getOnlineResources(), OnlineResource::getDescription))
                    .setResourceIdentifier(grab(gemini.getResourceIdentifiers(), ResourceIdentifier::getCode))
                    .setDataCentre(getDataCentre(gemini))
                    .setLocations(solrGeom(grab(gemini.getBoundingBoxes(), BoundingBox::getWkt)));
        }
        
        if(document instanceof EFDocument) {
            EFDocument ef = (EFDocument)document;
            
            String wktBBox = Optional.ofNullable(ef.getBoundingBox()).map(BoundingBox::getWkt).orElse(null);
            String wktGeom = Optional.ofNullable(ef.getGeometry()).map(Geometry::getWkt).orElse(null);
            toReturn.setLocations(solrGeom(Arrays.asList(wktBBox, wktGeom)));
        }
        return toReturn;
    }
    
    // Takes a list of wkt 
    private List<String> solrGeom(List<String> wktList) {
        return wktList
                .stream()
                .filter(Objects::nonNull)
                .map( w -> geometryService.toSolrGeometry(w))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private String getLicence(GeminiDocument document){
        boolean ogl = false;
        List<Keyword> licences = Optional.ofNullable(document.getUseLimitations())
            .orElse(Collections.emptyList());

        for (Keyword keyword : licences) {
            if ( !keyword.getUri().isEmpty()) {
                String uri = keyword.getUri();
                if (uri.startsWith(CEH_OGL_URL) || uri.startsWith(OGL_URL)) {
                    ogl = true;
                    break;
                }
            }
        }
        return codeLookupService.lookup("licence.isOgl", ogl);
    }

    private String getState(MetadataDocument document) {
        if (document.getMetadata() != null) {
            return document.getMetadata().getState();
        } else {
            return null;
        }
    }
    
    private List<String> getViews(MetadataDocument document) {
        Objects.requireNonNull(document);
        return Optional.ofNullable(document)
            .map(MetadataDocument::getMetadata)
            .map(m -> m.getIdentities(Permission.VIEW))
            .orElse(Collections.emptyList());       
    }
    
    private String getDataCentre(GeminiDocument document) {
        Optional<ResponsibleParty> dataCentre = document.getResponsibleParties()
            .stream()
            .filter(rp -> rp.getRole().equals("custodian") && rp.getOrganisationName().startsWith("EIDC"))
            .findFirst();
        
        if (dataCentre.isPresent()) {
            return "EIDCHub";
        } else {
            return "";
        }
    }
    
    // The following will iterate over a given collection (which could be null)
    // And grab a property off of each element in the collection.
    // If the supplied collection is null, this method will return an empty
    // list
    private <T> List<String> grab(Collection<T> list, Function<? super T, String> mapper ) {
        return Optional.ofNullable(list)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(mapper)
                        .map(Strings::emptyToNull)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
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
        private @Field List<String> altTitle;
        private @Field String description;
        private @Field String lineage;
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
        private @Field List<String> view;
        private @Field String dataCentre;
        
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
}
