package uk.ac.ceh.gateway.catalogue.ef;

import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {"start", "end"})
public class Lifespan {
    private LocalDate start, end;
}
