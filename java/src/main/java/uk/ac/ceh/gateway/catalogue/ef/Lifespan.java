package uk.ac.ceh.gateway.catalogue.ef;

import javax.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {"start", "end"})
public class Lifespan {
    private LocalDate start, end;
}