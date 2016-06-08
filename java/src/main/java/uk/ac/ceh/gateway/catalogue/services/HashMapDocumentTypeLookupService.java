package uk.ac.ceh.gateway.catalogue.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 *
 * @author cjohn
 */
public class HashMapDocumentTypeLookupService implements DocumentTypeLookupService {
    private final Map<String, Class<? extends MetadataDocument>> lookup;
    
    public HashMapDocumentTypeLookupService() {
        lookup = new HashMap<>();
    }
    
    @Override
    public HashMapDocumentTypeLookupService register(String name, Class<? extends MetadataDocument> metadataType) {
        lookup.put(name, metadataType);
        return this;
    }
    
    @Override
    public String getName(Class<? extends MetadataDocument> clazz) {
        for(Entry<String, Class<? extends MetadataDocument>> entry: lookup.entrySet()) {
            if(entry.getValue().isAssignableFrom(clazz)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException(clazz + " cannot be mapped to a known metadata document");
    }
    
    @Override
    public Class<? extends MetadataDocument> getType(String documentType) {
        Class<? extends MetadataDocument> type = lookup.get(documentType);
        if(type == null) {
            throw new IllegalArgumentException(documentType + ": does not have a corresponding class");
        }
        else {
            return type;
        }
    }
}
