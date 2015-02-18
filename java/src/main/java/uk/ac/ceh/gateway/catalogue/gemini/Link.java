package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Link {
    private final String title, href, associationType;

    @Builder
    private Link(String title, String href, String associationType) {
        this.title = nullToEmpty(title);
        this.href = nullToEmpty(href);
        this.associationType = nullToEmpty(associationType);
    }
}