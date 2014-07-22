package uk.ac.ceh.gateway.catalogue.gemini.elements;

import static com.google.common.base.Strings.nullToEmpty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class SpatialReferenceSystem {
   
    private static final String CODESPACE_EPSG = "urn:ogc:def:crs:EPSG";
    private static final Map<String, String> titleLookup;
    static {
        Map<String,String> temp = new HashMap<>();
        temp.put(String.format("%s%s", CODESPACE_EPSG, "27700"),"OSGB 1936 / British National Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "29900"),"TM65 / Irish National Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "29901"),"OSNI 1952 / Irish National Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "29902"),"TM65 / Irish Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "4326"),"WGS 84");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "CRS:84"),"WGS 84 longitude-latitude (CRS:84)");
        titleLookup = Collections.unmodifiableMap(temp);
    }
    private final String code, codeSpace;

    
    @Builder
    private SpatialReferenceSystem(String code, String codeSpace){
        this.code = nullToEmpty(code);
        this.codeSpace = nullToEmpty(codeSpace);
    }
    
    public String getTitle(){
        String toReturn = "";
        String key = String.format("%s%s",codeSpace,code);
        if(titleLookup.containsKey(key)){
            toReturn = titleLookup.get(key);
        }
        return toReturn;
    }
    
    public boolean isEmpty(){
        return nullToEmpty(this.code).isEmpty() && nullToEmpty(this.codeSpace).isEmpty();
    }
    
}

