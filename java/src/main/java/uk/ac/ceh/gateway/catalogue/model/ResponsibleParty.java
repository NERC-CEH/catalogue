package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import static com.google.common.base.Strings.nullToEmpty;

@Value
@With
@JsonIgnoreProperties({"roleDisplayName"})
public class ResponsibleParty {
    String individualName, organisationName, organisationIdentifier, role, email, phone, nameIdentifier;
    Address address;

    @Builder
    @JsonCreator
    private ResponsibleParty(
        @JsonProperty("individualName") String individualName,
        @JsonProperty("organisationName") String organisationName,
        @JsonProperty("organisationIdentifier") String organisationIdentifier,
        @JsonProperty("role") String role,
        @JsonProperty("email") String email,
        @JsonProperty("phone") String phone,
        @JsonProperty("nameIdentifier") String nameIdentifier,
        @JsonProperty("address") Address address) {
        this.individualName = nullToEmpty(individualName);
        this.organisationName = nullToEmpty(organisationName);
        this.organisationIdentifier = nullToEmpty(organisationIdentifier);
        this.role = nullToEmpty(role);
        this.email = nullToEmpty(email);
        this.phone = nullToEmpty(phone);
        this.nameIdentifier = nullToEmpty(nameIdentifier);
        this.address = (address == null || address.isEmpty()) ? null : address;
    }

    @JsonIgnore
    public boolean isOrcid() {
        return nameIdentifier.matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$");
    }

    @JsonIgnore
    public boolean isIsni() {
        return nameIdentifier.matches("^https://isni.org/isni/\\d{15}(X|\\d)$");
    }

    @JsonIgnore
    public boolean isRor() {
        return organisationIdentifier.matches("^https://ror.org/\\w{8,10}$");
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
                result.append(Character.toLowerCase(c));
            }
            else if(Character.isUpperCase(c) && !Character.isUpperCase(prevChar))
            {
                // insert space before start of word if camel case
                result.append( ' ' );
                result.append(Character.toLowerCase( c ));
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
        String deliveryPoint, city, administrativeArea, postalCode, country;

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
