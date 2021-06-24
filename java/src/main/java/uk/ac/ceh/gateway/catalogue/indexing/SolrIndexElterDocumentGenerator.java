package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrIndex;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Processes a ElterDocument and populates a SolrIndex object will all of the
 * bits of the document transferred. Ready to be indexed by Solr
 */
@Slf4j
@ToString
public class SolrIndexElterDocumentGenerator implements IndexGenerator<ElterDocument, SolrIndex> {
    private static final String OGL_PATTERN1 = ".*open-government-licence.*\\/plain$";
    private static final String OGL_PATTERN2 = ".*OGL.*\\/plain$";
    public static final String DEIMS_URL = "https://deims.org/";


    private final TopicIndexer topicIndexer;
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final CodeLookupService codeLookupService;

    public SolrIndexElterDocumentGenerator(
            TopicIndexer topicIndexer,
            SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex,
            CodeLookupService codeLookupService
    ) {
        this.topicIndexer = topicIndexer;
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        this.codeLookupService = codeLookupService;
        log.info("Creating {}", this);
    }

    @Override
    public SolrIndex generateIndex(ElterDocument document) {
        return metadataDocumentSolrIndex
                .generateIndex(document)
                .setAltTitle(document.getAlternateTitles())
                .setAuthorAffiliation(grab(document.getAuthors(), ResponsibleParty::getOrganisationName))
                .setAuthorName(grab(document.getAuthors(), ResponsibleParty::getIndividualName))
                .setAuthorOrcid(grab(document.getAuthors(), ResponsibleParty::getNameIdentifier))
                .setAuthorRor(grab(document.getAuthors(), ResponsibleParty::getOrganisationIdentifier))
                .setElterDeimsSite(grab(document.getDeimsSites(), DeimsSolrIndex::getTitle))
                .setElterDeimsSite(grab(document.getDeimsSites(), DeimsSolrIndex::getTitle))
                .setFunder(grab(document.getFunding(), Funding::getFunderName))
                .setGrant(grab(document.getFunding(), Funding::getAwardNumber))
                .setIncomingCitationCount(document.getIncomingCitationCount())
                .setIndividual(grab(document.getResponsibleParties(), ResponsibleParty::getIndividualName))
                .setLineage(document.getLineage())
                .setOrcid(grab(document.getResponsibleParties(), ResponsibleParty::getNameIdentifier))
                .setOrganisation(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
                .setResourceIdentifier(grab(document.getResourceIdentifiers(), ResourceIdentifier::getCode))
                .setResourceStatus(document.getResourceStatus())
                .setRor(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationIdentifier))
                .setRightsHolder(grab(document.getRightsHolders(), ResponsibleParty::getOrganisationName))
                .setSupplementalDescription(grab(document.getSupplemental(), Supplemental::getDescription))
                .setSupplementalName(grab(document.getSupplemental(), Supplemental::getName))
                .setVersion(document.getVersion())
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
                    return uri.matches(OGL_PATTERN1) || uri.matches(OGL_PATTERN2);
                });
    }

    private List<Keyword> getKeywordsFilteredByUrlFragment(MetadataDocument document, String... urlFragments) {
        return Optional.ofNullable(document.getAllKeywords())
                .orElse(Collections.emptyList())
                .stream()
                .filter(k -> Arrays.stream(urlFragments).anyMatch(urlFragment -> k.getUri().startsWith(urlFragment)))
                .collect(Collectors.toList());
    }
}
