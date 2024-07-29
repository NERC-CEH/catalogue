package uk.ac.ceh.gateway.catalogue.ef.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.UUID;

public class UuidAdapter extends XmlAdapter<String, UUID>{

    @Override
    public UUID unmarshal(String v) {
        if (v.isEmpty()) {
            return null;
        } else {
            return UUID.fromString(v);
        }
    }

    @Override
    public String marshal(UUID v) {
        return v.toString();
    }
}
