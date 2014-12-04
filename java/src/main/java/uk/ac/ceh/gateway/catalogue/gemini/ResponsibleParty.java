package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class ResponsibleParty {
    private final String individualName, organisationName, role, email;
    private final Address address;
    
    @Builder
    private ResponsibleParty(String individualName, String organisationName, String role, String email, Address address) {
        this.individualName = nullToEmpty(individualName);
        this.organisationName = nullToEmpty(organisationName);
        this.role = toTitlecase(nullToEmpty(role));
        this.email = nullToEmpty(email);
        this.address = (address == null || address.isEmpty()) ? null : address;    
    }
    
    private String toTitlecase(String camelCase) {
        StringBuilder result = new StringBuilder();

        // Pretend space before first character
        char prevChar = ' ';

        // insert space before capitals
        for(int i = 0; i < camelCase.length(); i++)
        {
            char c = camelCase.charAt(i);
            if( prevChar == ' ')
            {
                result.append(Character.toUpperCase(c));
            }
            else if(Character.isUpperCase(c) && !Character.isUpperCase(prevChar))
            {
                // insert space before start of word if camel case
                result.append( ' ' );
                result.append(Character.toUpperCase( c ));
            }
            else
            {
                result.append(c);
            }
            prevChar = c;
        }
        return result.toString();
    }
    
    @Value
    @JsonIgnoreProperties({"empty"})
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
        
        public boolean isEmpty() {
            return deliveryPoint.isEmpty() && city.isEmpty() && administrativeArea.isEmpty() && postalCode.isEmpty() && country.isEmpty();
        }
    }
}