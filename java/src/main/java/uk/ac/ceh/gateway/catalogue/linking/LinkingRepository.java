package uk.ac.ceh.gateway.catalogue.linking;

import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

public interface LinkingRepository {
    void delete(GeminiDocument document) throws DocumentLinkingException;
    void add(GeminiDocument document) throws DocumentLinkingException;
}