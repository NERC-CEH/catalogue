package uk.ac.ceh.ukeof.model.simple;

import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class Legislation extends Link{
    @XmlAttribute
    private String legislationId;
}