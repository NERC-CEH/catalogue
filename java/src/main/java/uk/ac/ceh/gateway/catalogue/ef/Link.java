package uk.ac.ceh.ukeof.model.simple;

import javax.xml.bind.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlSeeAlso({Link.TimedLink.class})
public class Link {
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String title, href;
    
    @XmlValue
    private String value;
    
    public String getValueOrFallback() {
        String toReturn = "";
        if (value != null && !value.isEmpty()) {
            toReturn = value;
        } else if (title != null && !title.isEmpty()) {
            toReturn = title;
        }
        return toReturn;
    }
    
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TimedLink extends Link {
        private Lifespan linkingTime;
    }
}