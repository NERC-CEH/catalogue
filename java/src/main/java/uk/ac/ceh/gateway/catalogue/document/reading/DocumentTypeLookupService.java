package uk.ac.ceh.gateway.catalogue.document.reading;

import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 * The following service is a registry for metadata types to an identifiable name
 */
public interface DocumentTypeLookupService {
    DocumentTypeLookupService register(String name, Class<? extends MetadataDocument> type);
    String getName(Class<? extends MetadataDocument> type);
    Class<? extends MetadataDocument> getType(String name);
}
