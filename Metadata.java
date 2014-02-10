package uk.ac.ceh.ukeof.model.simple;

import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "fileIdentifier",
    "selfUrl",
    "author",
    "authorDate",
    "publicationState",
    "quality"
})
public class Metadata {
    public enum State { 
        @XmlEnumValue("private") PRIVATE, 
        @XmlEnumValue("public") PUBLIC, 
        @XmlEnumValue("sensitive") SENSITIVE 
    }
    
    @NotNull
    private UUID fileIdentifier;
    private String selfUrl;
    private DateTime authorDate;
    @NotNull
    @Valid
    private ResponsibleParty author;
    @NotNull
    private State publicationState;
    private Quality quality;
    
    @Data
    @XmlType(propOrder = {
        "lineage",
        "conformanceResult"
    })
    public static class Quality {
        private String lineage;
        @Valid private ConformanceResult conformanceResult;
    }
    
    @Data
    @XmlType(propOrder = {
        "specification",
        "explanation",
        "pass"
    })
    public static class ConformanceResult {
        @NotNull private Link specification;
        private String explanation;
        @NotNull private String pass;
    }
}
