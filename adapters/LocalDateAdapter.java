package uk.ac.ceh.ukeof.model.simple.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.joda.time.LocalDate;
import org.slf4j.*;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate>{
    private final Logger logger = LoggerFactory.getLogger(LocalDateAdapter.class);

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        logger.debug("date string: {}", v);
        if (v.isEmpty()) {
            return null;
        } else {
            return new LocalDate(v);
        }
    }

    @Override
    public String marshal(LocalDate v) throws Exception {
        return v.toString();
    }
}