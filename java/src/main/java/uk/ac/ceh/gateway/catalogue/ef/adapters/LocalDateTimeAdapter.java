package uk.ac.ceh.gateway.catalogue.ef.adapters;

import java.time.LocalDateTime;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.*;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime>{
    private final Logger logger = LoggerFactory.getLogger(LocalDateTimeAdapter.class);

    @Override
    // XML => Java
    public LocalDateTime unmarshal(String v) throws Exception {
        logger.debug("unmarshal dateTime: {}", v);
        if (v == null || v.isEmpty()) {
            return null;
        } else {
            return LocalDateFactory.parseForDateTime(v);
        }
    }

    @Override
    // Java => XML
    public String marshal(LocalDateTime v) throws Exception {
        logger.debug("marshal dateTime: {}", v);
        if (v == null) {
            return null;
        } else {
            return v.toString();
        }
    }
}
