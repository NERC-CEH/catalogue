package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import lombok.Builder;

@Value
@JsonIgnoreProperties({"reference"})
public class SpatialReferenceSystem {
   
    private static final String CODESPACE_EPSG = "urn:ogc:def:crs:EPSG";
    private static final Map<String, String> titleLookup;
    static {
        Map<String,String> temp = new HashMap<>();
        temp.put(String.format("%s%s", CODESPACE_EPSG, "27700"),"OSGB 1936 / British National Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "29900"),"TM65 / Irish National Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "29901"),"OSNI 1952 / Irish National Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "29902"),"TM65 / Irish Grid");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "29903"),"TM75 / Irish Gridd");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "4326"),"WGS 84");
        temp.put(String.format("%s%s", CODESPACE_EPSG, "3857"),"Spherical mercator");
        titleLookup = Collections.unmodifiableMap(temp);
    }
    private final String code, codeSpace;

    
    @Builder
    @JsonCreator
    private SpatialReferenceSystem(
        @JsonProperty("code") String code,
        @JsonProperty("codeSpace") String codeSpace){
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
    
    public String getReference() {
        return String.format("%s::%s", codeSpace, code);
    }
}