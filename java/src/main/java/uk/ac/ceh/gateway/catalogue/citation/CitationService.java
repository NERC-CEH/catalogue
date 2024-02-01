package uk.ac.ceh.gateway.catalogue.citation;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.BIBTEX_SHORT;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RESEARCH_INFO_SYSTEMS_SHORT;

@Service
@Slf4j
@ToString
public class CitationService {
    public final String nercDoiPrefix;

    public CitationService(
            @Value("${doi.prefix}") String nercDoiPrefix
            ) {
        this.nercDoiPrefix = nercDoiPrefix;
        log.info("Creating {}", this);
            }

    /**
     * Generate a citation value for the current geminidocument. If the document
     * is not citable, just return null
     * @param geminiDocument The geminidocument to cite
     * @return Either a valid citation object or null
     */
    public Optional<Citation> getCitation(GeminiDocument geminiDocument) {

        Optional<GeminiDocument> doc = Optional.ofNullable(geminiDocument);

        Optional<ResourceIdentifier> citationResource = doc
            .map(GeminiDocument::getResourceIdentifiers)
            .orElse(Collections.emptyList())
            .stream()
            .filter((r)->r.getCodeSpace().equals("doi:"))
            .filter((r)->r.getCode().startsWith(nercDoiPrefix))
            .findFirst();

        //If is present and rest of document is valid
        if(citationResource.isPresent()) {
            ResourceIdentifier doi = citationResource.get();

            Optional<LocalDate> pubDate = doc
                .map(GeminiDocument::getDatasetReferenceDate)
                .map(DatasetReferenceDate::getPublicationDate);

            Optional<ResponsibleParty> publisher = getPublisher(geminiDocument);

            if (pubDate.isPresent() && publisher.isPresent()) {
                return Optional.of(
                        Citation
                        .builder()
                        .authors(   getAuthors(geminiDocument))
                        .doi(       doi.getCode())
                        .title(     geminiDocument.getTitle())
                        .year(      pubDate.get().getYear())
                        .publisher( publisher.get().getOrganisationName())
                        .resourceTypeGeneral( geminiDocument.getType())
                        .bibtex(    getInAlternateFormat(geminiDocument, BIBTEX_SHORT))
                        .ris(       getInAlternateFormat(geminiDocument, RESEARCH_INFO_SYSTEMS_SHORT))
                        .build()
                        );
            }
        }
        return Optional.empty();
    }

    protected URI getInAlternateFormat(GeminiDocument geminiDocument, String alternateFormat) {
        return UriComponentsBuilder.fromUriString(geminiDocument.getUri())
            .replacePath("documents/")
            .path(geminiDocument.getId())
            .path("/citation")
            .queryParam("format", alternateFormat)
            .build()
            .toUri();
    }

    protected Optional<ResponsibleParty> getPublisher(GeminiDocument geminiDocument) {
        return geminiDocument
            .getResponsibleParties()
            .stream()
            .filter((p) -> "publisher".equals(p.getRole()))
            .findFirst();
    }

    protected List<String> getAuthors(GeminiDocument geminiDocument) {
        return geminiDocument.getResponsibleParties()
            .stream()
            .filter((p) -> p.getRole().equals("author"))
            .map(ResponsibleParty::getIndividualName)
            .collect(Collectors.toList());
    }
}
