package uk.ac.ceh.gateway.catalogue.ef;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private LocalDateTime authorDate;
    @NotNull
    @Valid
    private ResponsibleParty author;
    @NotNull
    private State publicationState;
    private Quality quality;

    @Data
    @XmlType(propOrder = {
        "lineage"
    })
    public static class Quality {
        private String lineage;
    }
}
