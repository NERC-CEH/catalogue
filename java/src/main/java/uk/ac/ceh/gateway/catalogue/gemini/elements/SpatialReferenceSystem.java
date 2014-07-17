package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class SpatialReferenceSystem {
   
    private static final String CODESPACE_EPSG = "urn:ogc:def:crs:EPSG";
    private static final Map<String, String> titleLookup;
    static {
        Map<String,String> temp = new HashMap<>();
        temp.put(String.format("%s.%s", CODESPACE_EPSG, "27700"),"OSGB 1936 / British National Grid");
        temp.put(String.format("%s.%s", CODESPACE_EPSG, "29900"),"TM65 / Irish National Grid");
        temp.put(String.format("%s.%s", CODESPACE_EPSG, "29901"),"OSNI 1952 / Irish National Grid");
        temp.put(String.format("%s.%s", CODESPACE_EPSG, "4326"),"WGS 84");
        titleLookup = Collections.unmodifiableMap(temp);
    }
    private final String code, codeSpace;

    
    @Builder
    private SpatialReferenceSystem(String code, String codeSpace){
        this.code = code;
        this.codeSpace = codeSpace;
    }
    
    public String getTitle(){
        String toReturn = titleLookup.get(getURI());
        if(toReturn == null){
            toReturn = getURI();
        }
        return toReturn;
    }
    
    public String getURI(){
        return String.format("%s.%s",codeSpace,code);
    }
    
}

