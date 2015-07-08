package uk.ac.ceh.gateway.catalogue.ef.adapters;

import java.util.UUID;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UuidAdapter extends XmlAdapter<String, UUID>{

    @Override
    public UUID unmarshal(String v) throws Exception {
        if (v.isEmpty()) {
            return null;
        } else {
            return UUID.fromString(v);
        }
    }

    @Override
    public String marshal(UUID v) throws Exception {
        return v.toString();
    }
}