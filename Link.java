package uk.ac.ceh.ukeof.model.simple;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlSeeAlso({Link.TimedLink.class})
public class Link {
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String title, href;
    
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TimedLink extends Link {
        private Lifespan linkingTime;
    }
}