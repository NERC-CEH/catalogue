package uk.ac.ceh.ukeof.model.simple.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.joda.time.DateTime;
import org.slf4j.*;

public class DateTimeAdapter extends XmlAdapter<String, DateTime>{
    private final Logger logger = LoggerFactory.getLogger(DateTimeAdapter.class);

    @Override
    // XML => Java
    public DateTime unmarshal(String v) throws Exception {
        logger.debug("unmarshal dateTime: {}", v);
        if (v == null || v.isEmpty()) {
            return null;
        } else {
            return new DateTime(v);
        }
    }

    @Override
    // Java => XML
    public String marshal(DateTime v) throws Exception {
        logger.debug("marshal dateTime: {}", v);
        if (v == null) {
            return null;
        } else {
            return v.toString();
        }
    }
}