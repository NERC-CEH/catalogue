package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class ResponsibleParty {
    private final String individualName, organisationName, role, email;
    
    @Builder
    private ResponsibleParty(String individualName, String organisationName, String role, String email) {
        this.individualName = nullToEmpty(individualName);
        this.organisationName = nullToEmpty(organisationName);
        this.role = nullToEmpty(role);
        this.email = nullToEmpty(email);
    }
}