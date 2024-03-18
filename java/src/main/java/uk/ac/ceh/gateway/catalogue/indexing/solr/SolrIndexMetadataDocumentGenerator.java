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

    public static final String ASSIST_RESEARCH_THEMES_URL = "http://onto.nerc.ac.uk/CEHMD/assist-research-themes";
    public static final String ASSIST_TOPICS_URL = "http://onto.nerc.ac.uk/CEHMD/assist-topics";

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
    public static final String INMS_PROJECT_URL = "http://vocabs.ceh.ac.uk/inms/project";

    public static final String NC_ASSETS_URL = "http://vocabs.ceh.ac.uk/ncterms/asset";
    public static final String NC_CASE_STUDY_URL = "http://vocabs.ceh.ac.uk/ncterms/caseStudy";
    public static final String NC_DRIVERS_URL = "http://vocabs.ceh.ac.uk/ncterms/driver";
    public static final String NC_ECOSYSTEM_SERVICES_URL = "http://vocabs.ceh.ac.uk/ncterms/ecosystemService";
    public static final String NC_GEOGRAPHICAL_SCALE_URL = "http://vocabs.ceh.ac.uk/ncterms/geographical_scale";

    @SuppressWarnings("unused")
    public static final String SA_TAXON_URL = "http://vocabs.ceh.ac.uk/esb/taxon";

    private static final String UKSCAPE_RESEARCH_PROJECT_URL = "http://vocabs.ceh.ac.uk/ukscape/research-project";
    private static final String UKSCAPE_RESEARCH_THEME_URL = "http://vocabs.ceh.ac.uk/ukscape/research-theme";
    private static final String UKSCAPE_SCIENCE_CHALLENGE_URL = "http://vocabs.ceh.ac.uk/ukscape/science-challenge";
    private static final String UKSCAPE_SERVICE_URL = "http://vocabs.ceh.ac.uk/ukscape/service";

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
            .setAssistResearchThemes(grab(getKeywordsFilteredByUrlFragment(document, ASSIST_RESEARCH_THEMES_URL), Keyword::getValue))
            .setAssistTopics(grab(getKeywordsFilteredByUrlFragment(document, ASSIST_TOPICS_URL), Keyword::getValue))
            .setCatalogue(document.getCatalogue())
            .setCondition(getCondition(document))
            .setDescription(document.getDescription())
            .setDocumentType(getDocumentType(document))
            .setIdentifier(identifierService.generateFileId(document.getId()))
            .setImpCaMMPIssues(grab(getKeywordsFilteredByUrlFragment(document, IMP_CAMMP_ISSUES_URL), Keyword::getValue))
            .setImpDataType(grab(getKeywordsFilteredByUrlFragment(document, IMP_DATA_TYPE_URL), Keyword::getValue))
            .setImpScale(impScale(document))
            .setImpTopic(grab(getKeywordsFilteredByUrlFragment(document, IMP_TOPIC_URL, INMS_TOPIC_URL), Keyword::getValue))
            .setImpWaterPollutant(grab(getKeywordsFilteredByUrlFragment(document, IMP_WATER_POLLUTANT_URL, INMS_WATER_POLLUTANT_URL), Keyword::getValue))
            .setInmsDemonstrationRegion(grab(getKeywordsFilteredByUrlFragment(document, INMS_REGION_URL), Keyword::getValue))
            .setInmsProject(grab(getKeywordsFilteredByUrlFragment(document, INMS_PROJECT_URL), Keyword::getValue))
            .setKeyword(grab(document.getAllKeywords(), Keyword::getValue))
            .setLocations(getLocations(document))
            .setModelType(grab(getKeywordsFilteredByUrlFragment(document, INMS_MODEL_TYPE_URL), Keyword::getValue))
            .setNcAssets(grab(getKeywordsByVocabulary(document, NC_ASSETS_URL), Keyword::getValue))
            .setNcCaseStudy(grab(getKeywordsByVocabulary(document, NC_CASE_STUDY_URL), Keyword::getValue))
            .setNcDrivers(grab(getKeywordsByVocabulary(document, NC_DRIVERS_URL), Keyword::getValue))
            .setNcEcosystemServices(grab(getKeywordsByVocabulary(document, NC_ECOSYSTEM_SERVICES_URL), Keyword::getValue))
            .setNcGeographicalScale(grab(getKeywordsByVocabulary(document, NC_GEOGRAPHICAL_SCALE_URL), Keyword::getValue))
            .setRecordType(getRecordType(document))
            .setResourceType(codeLookupService.lookup("metadata.resourceType", document.getType()))
            .setState(getState(document))
            .setTitle(document.getTitle())
            .setUkscapeResearchTheme(grab(getKeywordsFilteredByUrlFragment(document, UKSCAPE_RESEARCH_THEME_URL), Keyword::getValue))
            .setUkscapeResearchProject(grab(getKeywordsFilteredByUrlFragment(document, UKSCAPE_RESEARCH_PROJECT_URL), Keyword::getValue))
            .setUkscapeScienceChallenge(grab(getKeywordsFilteredByUrlFragment(document, UKSCAPE_SCIENCE_CHALLENGE_URL), Keyword::getValue))
            .setUkscapeService(grab(getKeywordsFilteredByUrlFragment(document, UKSCAPE_SERVICE_URL), Keyword::getValue))
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

    private List<Keyword> getKeywordsByVocabulary(MetadataDocument document, String broaderUrl) {
        return Optional.ofNullable(document.getAllKeywords())
            .orElse(Collections.emptyList())
            .stream()
            .filter(k -> this.vocabularyService.isMember(broaderUrl, k.getUri()))
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
