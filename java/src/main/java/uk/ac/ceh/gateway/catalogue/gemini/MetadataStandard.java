package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
public class MetadataStandard {
    private String title, onlineLink, edition, conformity;
    private final LocalDate date;

    @Builder
    @JsonCreator
    private MetadataStandard(
        @JsonProperty("title") String title,
        @JsonProperty("edition") String edition,
        @JsonProperty("date") LocalDate date,
        @JsonProperty("onlineLink") String onlineLink,
        @JsonProperty("conformity") String conformity) {
        this.title = Strings.nullToEmpty(title);
        this.edition = Strings.nullToEmpty(edition);
        this.date = date;
        this.onlineLink = Strings.nullToEmpty(onlineLink);
        this.conformity = nullToEmpty(conformity);
    }
}
