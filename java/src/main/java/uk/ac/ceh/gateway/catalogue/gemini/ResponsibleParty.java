package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
@JsonIgnoreProperties({"roleDisplayName"})
public class ResponsibleParty {
    private final String individualName, organisationName, role, email;
    private final Address address;
    
    @Builder
    @JsonCreator
    private ResponsibleParty(
        @JsonProperty("individualName") String individualName,
        @JsonProperty("organisationName") String organisationName,
        @JsonProperty("role") String role,
        @JsonProperty("email") String email,
        @JsonProperty("address") Address address) {
        this.individualName = nullToEmpty(individualName);
        this.organisationName = nullToEmpty(organisationName);
        this.role = nullToEmpty(role);
        this.email = nullToEmpty(email);
        this.address = (address == null || address.isEmpty()) ? null : address;    
    }
    
    public String getRoleDisplayName() {
        return toTitlecase(role);
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
        @JsonCreator
        private Address(
            @JsonProperty("deliveryPoint") String deliveryPoint, 
            @JsonProperty("city") String city,
            @JsonProperty("administrativeArea") String administrativeArea,
            @JsonProperty("postalCode") String postalCode,
            @JsonProperty("country") String country) {
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