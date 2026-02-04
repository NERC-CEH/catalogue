package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.ObservedProperty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Processes a GeminiDocument and populates a SolrIndex object with all the
 * bits of the document transferred. Ready to be indexed by Solr
 */
@Slf4j
@ToString
public class SolrIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, SolrIndex> {
    private static final String OGL_PATTERN1 = ".*open-government-licence.*/plain$";
    private static final String OGL_PATTERN2 = ".*ogl.*/plain$";


    private final TopicIndexer topicIndexer;
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final CodeLookupService codeLookupService;

    public SolrIndexGeminiDocumentGenerator(
            TopicIndexer topicIndexer,
            SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex,
            CodeLookupService codeLookupService
    ) {
        this.topicIndexer = topicIndexer;
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        this.codeLookupService = codeLookupService;
        log.info("Creating");
    }

    @Override
    public SolrIndex generateIndex(GeminiDocument document) {
        return metadataDocumentSolrIndex
            .generateIndex(document)
            .setPublicationDate(document.getPublicationDate())
            .setAltTitle(document.getAlternateTitles())
            .setAuthorAffiliation(grab(document.getAuthors(), ResponsibleParty::getOrganisationName))
            .setAuthorGivenName(grab(document.getAuthors(), ResponsibleParty::getGivenName))
            .setAuthorFamilyName(grab(document.getAuthors(), ResponsibleParty::getFamilyName))
            .setAuthorFullName(grab(document.getAuthors(), ResponsibleParty::getFullName))
            .setAuthorOrcid(grab(document.getAuthors(), ResponsibleParty::getNameIdentifier))
            .setAuthorRor(grab(document.getAuthors(), ResponsibleParty::getOrganisationIdentifier))
            .setKeywordsInstrument(grab(document.getKeywordsInstrument(), Keyword::getValue))
            .setObservedPropertyValue(grab(getObservedProperty(document), ObservedProperty::getValue))
            .setObservedPropertyTitle(grab(getObservedProperty(document), ObservedProperty::getTitle))
            .setKeywordsPlace(grab(document.getKeywordsPlace(), Keyword::getValue))
            .setKeywordsProject(grab(document.getKeywordsProject(), Keyword::getValue))
            .setKeywordsTheme(grab(document.getKeywordsTheme(), Keyword::getValue))
            .setKeywordsOther(grab(document.getKeywordsOther(), Keyword::getValue))
            .setFunder(grab(document.getFunding(), Funding::getFunderName))
            .setGrant(grab(document.getFunding(), Funding::getAwardNumber))
            .setIncomingCitationCount(document.getIncomingCitationCount())
            .setGivenName(grab(document.getResponsibleParties(), ResponsibleParty::getGivenName))
            .setFamilyName(grab(document.getResponsibleParties(), ResponsibleParty::getFamilyName))
            .setFullName(grab(document.getResponsibleParties(), ResponsibleParty::getFullName))
            .setLicence(getLicence(document))
            .setLineage(document.getLineage())
            .setOrcid(grab(document.getResponsibleParties(), ResponsibleParty::getNameIdentifier))
            .setOrganisation(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
            .setAvailability(document.getAvailability())
            .setRor(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationIdentifier))
            .setRightsHolder(grab(document.getRightsHolders(), ResponsibleParty::getOrganisationName))
            .setSupplementalDescription(grab(document.getSupplemental(), Supplemental::getDescription))
            .setSupplementalName(grab(document.getSupplemental(), Supplemental::getName))
            .setTopic(topicIndexer.index(document))
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

    private List<ObservedProperty> getObservedProperty(GeminiDocument document) {
        List<ObservedProperty> list = new ArrayList<>();
        Optional.ofNullable(document.getFileset())
            .orElse(Collections.emptyList())
            .forEach(fileset -> {
                list.addAll(fileset.getObservedProperty());
            });

        return list;
    }
}
