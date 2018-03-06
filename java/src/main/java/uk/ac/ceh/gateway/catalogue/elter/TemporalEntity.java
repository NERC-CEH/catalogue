package uk.ac.ceh.gateway.catalogue.elter;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class TemporalEntity {
    private Date instant;
    private List<Date> interval;
}