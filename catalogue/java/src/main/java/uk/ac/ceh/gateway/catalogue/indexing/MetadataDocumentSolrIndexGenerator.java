package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
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
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * The following class is responsible for taking a gemini document and creating 
 * beans which are solr indexable
 * @author cjohn
 */
public class MetadataDocumentSolrIndexGenerator implements IndexGenerator<MetadataDocument, SolrIndex> {
    private static final String OGL_URL = "http://www.nationalarchives.gov.uk/doc/open-government-licence";
    private static final String CEH_OGL_URL = "http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence";
    private final TopicIndexer topicIndexer;
    private final CodeLookupService codeLookupService;
    private final DocumentIdentifierService identifierService;
    private final SolrGeometryService geometryService;
    
    public MetadataDocumentSolrIndexGenerator(TopicIndexer topicIndexer, CodeLookupService codeLookupService, DocumentIdentifierService identifierService, SolrGeometryService geometryService) {
        this.topicIndexer = topicIndexer;
        this.codeLookupService = codeLookupService;
        this.identifierService = identifierService;
        this.geometryService = geometryService;
    }

    @Override
    public SolrIndex generateIndex(MetadataDocument document) {
        List<String> locations = new ArrayList<>();
        SolrIndex toReturn = new SolrIndex()
                .setDescription(document.getDescription())
                .setTitle(document.getTitle())
                .setIdentifier(identifierService.generateFileId(document.getId()))
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
                    .setDataCentre(getDataCentre(gemini));
            locations.addAll(solrGeom(grab(gemini.getBoundingBoxes(), BoundingBox::getWkt)));
        }
        
        if(document instanceof BaseMonitoringType) {
            BaseMonitoringType ef = (BaseMonitoringType)document;
            
            locations.addAll(solrGeom(grab(ef.getBoundingBoxes(), BaseMonitoringType.BoundingBox::getWkt)));
        }
        
        if(document instanceof Facility) {
            Facility ef = (Facility)document;                    
            locations.addAll(solrGeom(grab(Arrays.asList(ef.getGeometry()), Geometry::getValue)));
        }
        return toReturn.setLocations(locations);
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
}
