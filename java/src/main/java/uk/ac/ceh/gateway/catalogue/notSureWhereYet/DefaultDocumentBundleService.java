package uk.ac.ceh.gateway.catalogue.notSureWhereYet;

import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentBundleService;

/**
 *
 * @author cjohn
 */
@Service
public class DefaultDocumentBundleService implements DocumentBundleService<Metadata, MetadataInfo, Metadata>{

    @Override
    public Metadata bundle(Metadata document, MetadataInfo info) {
        document.setMetadata(info);
        return document;
    }    
}
