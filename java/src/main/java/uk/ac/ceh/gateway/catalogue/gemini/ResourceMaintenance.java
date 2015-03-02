package uk.ac.ceh.gateway.catalogue.gemini;

import com.google.common.base.Strings;
import lombok.Value;

@Value
public class ResourceMaintenance {
    private String frequencyOfUpdate, note; 

    public ResourceMaintenance(String frequencyOfUpdate, String note) {
        this.frequencyOfUpdate = Strings.nullToEmpty(frequencyOfUpdate);
        this.note = Strings.nullToEmpty(note);
    }
}