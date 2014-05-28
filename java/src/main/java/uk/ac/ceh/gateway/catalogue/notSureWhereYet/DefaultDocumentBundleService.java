package uk.ac.ceh.gateway.catalogue.notSureWhereYet;

import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentBundleService;

/**
 *
 * @author cjohn
 */
@Service
public class DefaultDocumentBundleService implements DocumentBundleService<GeminiDocument, MetadataInfo, GeminiDocument>{

    @Override
    public GeminiDocument bundle(GeminiDocument document, MetadataInfo info) {
        document.setMetadata(info);
        return document;
    }    
}
