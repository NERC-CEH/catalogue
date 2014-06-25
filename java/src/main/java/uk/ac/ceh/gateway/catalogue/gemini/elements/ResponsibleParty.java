package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class ResponsibleParty {
    private final String individualName, organisationName, role, email;
    private final Address address;
    
    @Builder
    private ResponsibleParty(String individualName, String organisationName, String role, String email, Address address) {
        this.individualName = nullToEmpty(individualName);
        this.organisationName = nullToEmpty(organisationName);
        this.role = nullToEmpty(role);
        this.email = nullToEmpty(email);
        this.address = address;
    }
    
    @Value
    public static class Address {
        private final String deliveryPoint, city, administrativeArea, postalCode, country;
        
        @Builder
        private Address(String deliveryPoint, String city, String administrativeArea, String postalCode, String country) {
            this.deliveryPoint = nullToEmpty(deliveryPoint);
            this.city = nullToEmpty(city);
            this.administrativeArea = nullToEmpty(administrativeArea);
            this.postalCode = nullToEmpty(postalCode);
            this.country = nullToEmpty(country);
        }
    }
}