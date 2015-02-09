package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

@Value
public class StateTranslatingMetadataInfoFactory implements TerraCatalogDocumentInfoFactory<MetadataInfo>{
    @Getter(AccessLevel.NONE)
    private final Map<String, String> stateTranslation;

    public StateTranslatingMetadataInfoFactory() {
        this.stateTranslation = new HashMap<>();
        this.stateTranslation.put("default", "draft");
    }
    
    public void put(String protection, String state) {
        stateTranslation.put(protection.toLowerCase(), state.toLowerCase());
    }

    @Override
    public MetadataInfo getDocumentInfo(GeminiDocument document, TerraCatalogExt ext) {
        MetadataInfo toReturn = new MetadataInfo()
            .setRawType("application/xml")
            .setState(translate(ext.getProtection()))
            .setDocumentType("GEMINI_DOCUMENT");
        toReturn.addPermission(Permission.VIEW, ext.getOwnerGroup().toLowerCase());
        return toReturn;
    }
    
    private String translate(String terraCatalogStatus) {
        if (stateTranslation.containsKey(terraCatalogStatus)) {
            return stateTranslation.get(terraCatalogStatus);
        } else {
            return stateTranslation.get("default");
        }
    }

}