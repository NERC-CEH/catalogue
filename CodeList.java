package uk.ac.ceh.ukeof.model.simple;

import javax.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CodeList {
    @XmlAttribute
    private String codeList, codeListValue;
}