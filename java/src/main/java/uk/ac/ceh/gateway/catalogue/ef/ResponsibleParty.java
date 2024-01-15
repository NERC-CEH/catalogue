package uk.ac.ceh.gateway.catalogue.ef;

import java.util.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "individualName",
    "position",
    "organisationName",
    "postalAddress",
    "telephone",
    "email",
    "role",
    "onlineResource",
    "personalIdentifiers"
})
public class ResponsibleParty {
    private String individualName, position, telephone, email;
    
    @NotNull
    private String organisationName;
    
    private Address postalAddress;
    private Link onlineResource, role;
    
    @XmlElement(name = "personalIdentifier")
    private List<PersonalIdentifier> personalIdentifiers = new ArrayList<>();
    
    @Data
    @XmlType(propOrder = {"street", "postalArea", "administrativeArea", "country", "postcode"})
    public static class Address {
        private String street, administrativeArea, country, postcode;
        private List<String> postalArea = new ArrayList<>();
    }
    
    @Data
    public static class PersonalIdentifier {
        @XmlAttribute
        private String schemeName;
        
        @XmlValue
        private String identifier;
    }
}
