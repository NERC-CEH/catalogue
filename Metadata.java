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
    "publicationState"
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
}
