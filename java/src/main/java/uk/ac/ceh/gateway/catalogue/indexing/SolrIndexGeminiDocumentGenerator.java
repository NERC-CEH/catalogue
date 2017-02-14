package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * Processes a GeminiDocument and populates a SolrIndex object will all of the
 * bits of the document transferred. Ready to be indexed by Solr
 * @author cjohn
 */
public class SolrIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, SolrIndex> {
    private static final String OGL_URL = "http://www.nationalarchives.gov.uk/doc/open-government-licence";
    private static final String CEH_OGL_URL = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/cehOGL";
    private static final String OTHER_OGL_URL = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence";
    public static final String IMP_CAMMP_ISSUES_URL = "http://vocabs.ceh.ac.uk/imp/ci/";
    public static final String IMP_DATA_TYPE_URL = "http://vocabs.ceh.ac.uk/imp/dt/";
    public static final String IMP_SCALE_URL = "http://vocabs.ceh.ac.uk/imp/scale/";
    public static final String IMP_TOPIC_URL = "http://vocabs.ceh.ac.uk/imp/topic/";
    public static final String IMP_WATER_POLLUTANT_URL = "http://vocabs.ceh.ac.uk/imp/wp/";
    
    private final TopicIndexer topicIndexer;
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;
    private final CodeLookupService codeLookupService;

    @Autowired
    public SolrIndexGeminiDocumentGenerator(TopicIndexer topicIndexer, SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex, SolrGeometryService geometryService, CodeLookupService codeLookupService) {
        this.topicIndexer = topicIndexer;
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        this.geometryService = geometryService;
        this.codeLookupService = codeLookupService;
    }
    
    @Override
    public SolrIndex generateIndex(GeminiDocument document) {
        return metadataDocumentSolrIndex
                .generateIndex(document)
                .setTopic(topicIndexer.index(document))
                .setAltTitle(document.getAlternateTitles())
                .setLineage(document.getLineage())
                .setLicence(getLicence(document))
                .setOrganisation(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
                .setIndividual(grab(document.getResponsibleParties(), ResponsibleParty::getIndividualName))
                .setOnlineResourceName(grab(document.getOnlineResources(), OnlineResource::getName))
                .setOnlineResourceDescription(grab(document.getOnlineResources(), OnlineResource::getDescription))
                .setResourceIdentifier(grab(document.getResourceIdentifiers(), ResourceIdentifier::getCode))
                .setKeyword(grab(document.getAllKeywords(), Keyword::getValue))
                .addLocations(geometryService.toSolrGeometry(grab(document.getBoundingBoxes(), BoundingBox::getWkt)))
                .setImpCaMMPIssues(grab(getKeywordsFilteredByUrlFragment(document, IMP_CAMMP_ISSUES_URL), Keyword::getValue))
                .setImpDataType(grab(getKeywordsFilteredByUrlFragment(document, IMP_DATA_TYPE_URL), Keyword::getValue))
                .setImpScale(grab(getKeywordsFilteredByUrlFragment(document, IMP_SCALE_URL), Keyword::getValue))
                .setImpTopic(grab(getKeywordsFilteredByUrlFragment(document, IMP_TOPIC_URL), Keyword::getValue))
                .setImpWaterPollutant(grab(getKeywordsFilteredByUrlFragment(document, IMP_WATER_POLLUTANT_URL), Keyword::getValue))
            ;
    }

    private String getLicence(GeminiDocument document){
        return codeLookupService.lookup("licence.isOgl", hasOglLicence(document));
    }
    
    private boolean hasOglLicence(GeminiDocument document) {
        return Optional.ofNullable(document.getUseConstraints())
            .orElse(Collections.emptyList())
            .stream()
            .filter(k -> !k.getUri().isEmpty())
            .anyMatch(k -> {
                String uri = k.getUri();
                return uri.startsWith(OTHER_OGL_URL)
                    || uri.startsWith(CEH_OGL_URL)
                    || uri.startsWith(OGL_URL);  
            });
    }
    
    private List<Keyword> getKeywordsFilteredByUrlFragment(GeminiDocument document, String urlFragment) {
        return document
            .getAllKeywords()
            .stream()
            .filter(k -> {
                return k.getUri().startsWith(urlFragment);
            })
            .collect(Collectors.toList());
    }
}
