package uk.ac.ceh.gateway.catalogue.postprocess;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.citation.CitationService;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

/**
 * Defines a post processing service which can be used adding additional
 * information to a Gemini Document
 */
@Slf4j
@ToString
public class GeminiDocumentPostProcessingService implements PostProcessingService<GeminiDocument> {
    private final CitationService citationService;
    private final DataciteService dataciteService;
    // TODO: Replace post-processing with calls to these services as Freemarker global variables
    public GeminiDocumentPostProcessingService(
        CitationService citationService,
        DataciteService dataciteService
    ) {
        this.citationService = citationService;
        this.dataciteService = dataciteService;
        log.info("Creating {}", this);
    }

    @Override
    public void postProcess(GeminiDocument document) {
        citationService
            .getCitation(document)
            .ifPresent(document::setCitation);

        document.setDataciteMintable(dataciteService.isDataciteMintable(document));
        document.setDatacitable(dataciteService.isDatacitable(document, true));
    }
}
