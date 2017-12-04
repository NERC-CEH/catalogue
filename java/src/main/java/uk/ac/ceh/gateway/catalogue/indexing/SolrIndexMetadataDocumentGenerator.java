package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The following class is responsible for taking a metadata document and creating 
 * beans which are solr indexable
 */
@AllArgsConstructor
@Slf4j
public class SolrIndexMetadataDocumentGenerator implements IndexGenerator<MetadataDocument, SolrIndex> {
    public static final String IMP_CAMMP_ISSUES_URL = "http://vocabs.ceh.ac.uk/imp/ci/";
    public static final String IMP_DATA_TYPE_URL = "http://vocabs.ceh.ac.uk/imp/dt/";
    public static final String IMP_SCALE_URL = "http://vocabs.ceh.ac.uk/imp/scale/";
    public static final String IMP_TOPIC_URL = "http://vocabs.ceh.ac.uk/imp/topic/";
    public static final String IMP_WATER_POLLUTANT_URL = "http://vocabs.ceh.ac.uk/imp/wp/";
    
    public static final String INMS_SCALE_URL = "http://vocabs.ceh.ac.uk/inms/scale/";
    public static final String INMS_TOPIC_URL = "http://vocabs.ceh.ac.uk/inms/topic/";
    public static final String INMS_MODEL_TYPE_URL = "http://vocabs.ceh.ac.uk/inms/model_type";
    public static final String INMS_WATER_POLLUTANT_URL = "http://vocabs.ceh.ac.uk/inms/wp/";
    public static final String INMS_REGION_URL = "http://vocabs.ceh.ac.uk/inms/region";
    
    private final CodeLookupService codeLookupService;
    private final DocumentIdentifierService identifierService;

    @Override
    public SolrIndex generateIndex(MetadataDocument document) {
        log.info("{} is a {}", document.getId(), codeLookupService.lookup("metadata.resourceType", document.getType()));
        return new SolrIndex()
            .setDescription(document.getDescription())
            .setTitle(document.getTitle())
            .setIdentifier(identifierService.generateFileId(document.getId()))
            .setResourceType(codeLookupService.lookup("metadata.resourceType", document.getType()))
            .setState(getState(document))
            .setView(getViews(document))
            .setCatalogue(document.getCatalogue())
            .setDocumentType(getDocumentType(document))
            .setImpCaMMPIssues(grab(getKeywordsFilteredByUrlFragment(document, IMP_CAMMP_ISSUES_URL), Keyword::getValue))
            .setImpDataType(grab(getKeywordsFilteredByUrlFragment(document, IMP_DATA_TYPE_URL), Keyword::getValue))
            .setImpScale(impScale(document))
            .setImpTopic(grab(getKeywordsFilteredByUrlFragment(document, IMP_TOPIC_URL, INMS_TOPIC_URL), Keyword::getValue))
            .setImpWaterPollutant(grab(getKeywordsFilteredByUrlFragment(document, IMP_WATER_POLLUTANT_URL, INMS_WATER_POLLUTANT_URL), Keyword::getValue))
            .setInmsDemonstrationRegion(grab(getKeywordsFilteredByUrlFragment(document, INMS_REGION_URL), Keyword::getValue))
            .setModelType(grab(getKeywordsFilteredByUrlFragment(document, INMS_MODEL_TYPE_URL), Keyword::getValue));
    }
    
    private String getState(MetadataDocument document) {
        return Optional.ofNullable(document)
            .map(MetadataDocument::getMetadata)
            .map(MetadataInfo::getState)
            .orElse("");
    }
    
    private String getDocumentType(MetadataDocument document) {
        return Optional.ofNullable(document)
            .map(MetadataDocument::getMetadata)
            .map(MetadataInfo::getDocumentType)
            .orElse("");
    }
    
    private List<String> getViews(MetadataDocument document) {
        return Optional.ofNullable(document)
            .map(MetadataDocument::getMetadata)
            .map(m -> m.getIdentities(Permission.VIEW))
            .orElse(Collections.emptyList());       
    }
    
    // The following will iterate over a given collection (which could be null)
    // And grab a property off of each element in the collection.
    // If the supplied collection is null, this method will return an empty
    // list
    public static <T> List<String> grab(Collection<T> list, Function<? super T, String> mapper ) {
        return Optional.ofNullable(list)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(mapper)
                        .map(Strings::emptyToNull)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
    }

    public static <T> List<String> grab(T item, Function<? super T, String> mapper ) {
        return grab(Arrays.asList(item), mapper);
    }
    
    private List<Keyword> getKeywordsFilteredByUrlFragment(MetadataDocument document, String... urlFragments) {
        return Optional.ofNullable(document.getAllKeywords())
                .orElse(Collections.emptyList())
                .stream()
                .filter(k -> Arrays.stream(urlFragments).anyMatch(urlFragment -> k.getUri().startsWith(urlFragment)))
                .collect(Collectors.toList());
    }
    
    private List<String> impScale(MetadataDocument document) {
        List<String> toReturn = grab(
            getKeywordsFilteredByUrlFragment(document, IMP_SCALE_URL, INMS_SCALE_URL),
            Keyword::getValue
        );
        
        if (document instanceof Model) {
            String applicationScale = ((Model) document).getApplicationScale();
            toReturn.add(applicationScale);
        } else if (document instanceof CehModelApplication) {
            CehModelApplication application = (CehModelApplication) document;
            Optional.ofNullable(application.getModelInfos())
                .orElse(Collections.emptyList())
                .stream()
                .filter(mi -> mi.getSpatialExtentOfApplication() != null && !mi.getSpatialExtentOfApplication().isEmpty())
                .forEach(mi -> toReturn.add(mi.getSpatialExtentOfApplication()));
        }
        return toReturn;
    }
}
