package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;

@Value
public class StateTranslatingMetadataInfoFactory implements TerraCatalogDocumentInfoFactory<MetadataInfo>{
    @Getter(AccessLevel.NONE)
    private final Map<String, String> stateTranslation;

    public StateTranslatingMetadataInfoFactory(Map<String, String> stateTranslation) {
        this.stateTranslation = stateTranslation;
    }

    @Override
    public MetadataInfo getDocumentInfo(GeminiDocument document, TerraCatalogExt ext) {
        return new MetadataInfo(ext.getOwnerGroup(), null, translate(ext.getStatus()));
    }
    
    private String translate(String terraCatalogStatus) {
        if (stateTranslation.containsKey(terraCatalogStatus)) {
            return stateTranslation.get(terraCatalogStatus);
        } else {
            return stateTranslation.get("default");
        }
    }

}