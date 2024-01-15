package uk.ac.ceh.gateway.catalogue.ef;

import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

@Data
@EqualsAndHashCode(exclude = {"title","value"})
@Accessors(chain = true)
@XmlSeeAlso({Link.TimedLink.class})
public class Link {
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String title, href;
    
    @XmlValue
    private String value;
    
    public String getDisplayText() {
        return Arrays.asList(value, title, href)
                .stream()
                .filter(Objects::nonNull)
                .filter(s-> !s.isEmpty())
                .findFirst()
                .orElse("No content in link");
    }

    public Keyword asKeyword() {
        return Keyword
            .builder()
            .value(value)
            .URI(href)
            .build();
    }
    
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TimedLink extends Link {
        private Lifespan linkingTime;
    }
}
