package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;
import lombok.Value;
import lombok.Builder;

@Value
@JsonIgnoreProperties({"coupleResource", "internal"})
public class ResourceIdentifier {
    private final static String DOI_CODE_SPACE = "doi:";
    private final String code, codeSpace, version;
    
    @Builder
    @JsonCreator
    private ResourceIdentifier(
        @JsonProperty("code") String code,
        @JsonProperty("codeSpace") String codeSpace,
        @JsonProperty("version") String version) {
        this.code = nullToEmpty(code);
        this.codeSpace = nullToEmpty(codeSpace);
        this.version = nullToEmpty(version);
    }
    
    @JsonIgnore
    public String getCoupledResource() {
        if (DOI_CODE_SPACE.equals(codeSpace)) {
            return format("https://doi.org/%s", code);
        }
        else {
            return format("%s%s", codeSpace, code);
        }
    }
   
}