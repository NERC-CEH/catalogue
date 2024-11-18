package uk.ac.ceh.gateway.catalogue.indexing.solr;

import com.google.common.base.Strings;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyFacet;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The following class is responsible for taking a metadata document and creating
 * beans which are solr indexable
 */
@SuppressWarnings("HttpUrlsUsage")
@Slf4j
@ToString
public class SolrIndexMetadataDocumentGenerator implements IndexGenerator<MetadataDocument, SolrIndex> {

    public static final String IMP_CAMMP_ISSUES_URL = "http://vocabs.ceh.ac.uk/imp/ci/";
    public static final String IMP_SCALE_URL = "http://vocabs.ceh.ac.uk/imp/scale/";

    public static final String INMS_SCALE_URL = "http://vocabs.ceh.ac.uk/inms/scale/";

    @SuppressWarnings("unused")
    public static final String SA_TAXON_URL = "http://vocabs.ceh.ac.uk/esb/taxon";

    private final CodeLookupService codeLookupService;
    private final DocumentIdentifierService identifierService;
    private final VocabularyService vocabularyService;

    public SolrIndexMetadataDocumentGenerator(
            CodeLookupService codeLookupService,
            DocumentIdentifierService identifierService,
            VocabularyService vocabularyService
    ) {
        this.codeLookupService = codeLookupService;
        this.identifierService = identifierService;
        this.vocabularyService = vocabularyService;
        log.info("Creating {}", this);
    }

    @Override
    public SolrIndex generateIndex(MetadataDocument document) {
        log.info("{} is a {}, {}", document.getId(), codeLookupService.lookup("metadata.resourceType", document.getType()), codeLookupService.lookup("metadata.recordType", document.getType()));
        return new SolrIndex()
            .setAssistResearchThemes(grab(getKeywordsByVocabulary(document, VocabularyFacet.ASSIST_RESEARCH_THEMES.getFacetName()), Keyword::getValue))
            .setAssistTopics(grab(getKeywordsByVocabulary(document, VocabularyFacet.ASSIST_TOPICS.getFacetName()), Keyword::getValue))
            .setCatalogue(document.getCatalogue())
            .setCondition(getCondition(document))
            .setDescription(document.getDescription())
            .setDocumentType(getDocumentType(document))
            .setIdentifier(identifierService.generateFileId(document.getId()))
            .setImpCaMMPIssues(grab(getKeywordsFilteredByUrlFragment(document, IMP_CAMMP_ISSUES_URL), Keyword::getValue))
            .setImpDataType(grab(getKeywordsByVocabulary(document, VocabularyFacet.IMP_DATE_TYPE.getFacetName()), Keyword::getValue))
            .setImpScale(impScale(document))
            .setImpTopic(grab(getKeywordsByVocabulary(document, VocabularyFacet.TOPIC.getFacetName()), Keyword::getValue))
            .setImpWaterPollutant(grab(getKeywordsByVocabulary(document, VocabularyFacet.WATER_POLLUTANT.getFacetName()), Keyword::getValue))
            .setInmsDemonstrationRegion(grab(getKeywordsByVocabulary(document, VocabularyFacet.INMS_DEMONSTRATION_REGION.getFacetName()), Keyword::getValue))
            .setInmsProject(grab(getKeywordsByVocabulary(document, VocabularyFacet.INMS_PROJECT.getFacetName()), Keyword::getValue))
            .setKeyword(grab(document.getAllKeywords(), Keyword::getValue))
            .setLocations(getLocations(document))
            .setModelType(grab(getKeywordsByVocabulary(document, VocabularyFacet.MODEL_TYPE.getFacetName()), Keyword::getValue))
            .setRecordType(getRecordType(document))
            .setResourceType(codeLookupService.lookup("metadata.resourceType", document.getType()))
            .setState(getState(document))
            .setTitle(document.getTitle())
            .setUkcehResearchTheme(grab(getKeywordsByVocabulary(document, VocabularyFacet.UKCEH_RESEARCH_THEME.getFacetName()), Keyword::getValue))
            .setUkcehResearchProject(grab(getKeywordsByVocabulary(document, VocabularyFacet.UKCEH_RESEARCH_PROJECT.getFacetName()), Keyword::getValue))
            .setUkcehScienceChallenge(grab(getKeywordsByVocabulary(document, VocabularyFacet.UKCEH_SCIENCE_CHALLENGE.getFacetName()), Keyword::getValue))
            .setUkcehService(grab(getKeywordsByVocabulary(document, VocabularyFacet.UKCEH_SERVICE.getFacetName()), Keyword::getValue))
            .setView(getViews(document))
            ;
    }

    private String getRecordType(MetadataDocument document) {
        log.debug("Catalogue: {}", document.getCatalogue());
        if (document.getCatalogue().equals("eidc")) {
            return codeLookupService.lookup("metadata.recordType", document.getType());
        } else {
            return codeLookupService.lookup("metadata.resourceType", document.getType());
        }
    }

    private String getCondition(MetadataDocument document) {
        if(document instanceof ErammpDatacube){
            return ((ErammpDatacube) document).getCondition();
        } else {
            return null;
        }
    }

    private List<String> getLocations(MetadataDocument document) {
        if (document instanceof WellKnownText) {
            return ((WellKnownText) document).getWKTs();
        } else {
            return Collections.emptyList();
        }
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
        return grab(Collections.singletonList(item), mapper);
    }

    private List<Keyword> getKeywordsFilteredByUrlFragment(MetadataDocument document, String... urlFragments) {
        return Optional.ofNullable(document.getAllKeywords())
                .orElse(Collections.emptyList())
                .stream()
                .filter(k -> Arrays.stream(urlFragments).anyMatch(urlFragment -> k.getUri().startsWith(urlFragment)))
                .collect(Collectors.toList());
    }

    private List<Keyword> getKeywordsByVocabulary(MetadataDocument document, String vocabularyFacet) {
        return Optional.ofNullable(document.getAllKeywords())
            .orElse(Collections.emptyList())
            .stream()
            .filter(k -> this.vocabularyService.isMember(vocabularyFacet, k.getUri()))
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
        } else if (document instanceof CehModelApplication application) {
            Optional.ofNullable(application.getModelInfos())
                .orElse(Collections.emptyList())
                .stream()
                .filter(mi -> mi.getSpatialExtentOfApplication() != null && !mi.getSpatialExtentOfApplication().isEmpty())
                .forEach(mi -> toReturn.add(mi.getSpatialExtentOfApplication()));
        }
        return toReturn;
    }
}
