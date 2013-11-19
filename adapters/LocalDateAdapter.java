package uk.ac.ceh.ukeof.model.simple.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.joda.time.LocalDate;

/**
 *
 * @author rjsc
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate>{

    @Override
    public LocalDate unmarshal(String v) throws Exception {
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