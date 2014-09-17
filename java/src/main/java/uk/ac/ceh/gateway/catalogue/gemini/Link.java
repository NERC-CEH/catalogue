package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Value;
import lombok.experimental.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Link {
    private final String title, href;

    @Builder
    private Link(String title, String href) {
        this.title = nullToEmpty(title);
        this.href = nullToEmpty(href);
    }
}