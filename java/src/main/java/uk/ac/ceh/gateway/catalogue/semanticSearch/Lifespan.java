package uk.ac.ceh.gateway.catalogue.semanticSearch;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {"start", "end"})
public class Lifespan {
    private LocalDate start, end;
}
