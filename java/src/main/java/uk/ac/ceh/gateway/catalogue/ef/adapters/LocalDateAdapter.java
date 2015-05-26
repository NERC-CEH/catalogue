package uk.ac.ceh.ukeof.model.simple.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.joda.time.LocalDate;
import org.slf4j.*;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate>{
    private final Logger logger = LoggerFactory.getLogger(LocalDateAdapter.class);

    @Override
    // XML => Java
    public LocalDate unmarshal(String v) throws Exception {
        logger.debug("unmarshal localDate: {}", v);
        if (v == null || v.isEmpty()) {
            return null;
        } else {
            return new LocalDate(v);
        }
    }

    @Override
    // Java => XML
    public String marshal(LocalDate v) throws Exception {
        logger.debug("marshal localDate: {}", v);
        if (v == null) {
            return null;
        } else {
            return v.toString();
        }
    }
}