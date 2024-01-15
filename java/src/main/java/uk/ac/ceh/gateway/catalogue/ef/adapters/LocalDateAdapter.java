package uk.ac.ceh.gateway.catalogue.ef.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import org.slf4j.*;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate>{
    private final Logger logger = LoggerFactory.getLogger(LocalDateAdapter.class);

    @Override
    // XML => Java
    public LocalDate unmarshal(String v) throws Exception {
        logger.debug("unmarshal localDate: {}", v);
        if (v == null || v.isEmpty()) {
            return null;
        } else {
            return LocalDateFactory.parse(v);
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
