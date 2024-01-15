package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty.Address;

public class ResponsiblePartyConverter {
    private static final String INDIVIDUAL_NAME = "gmd:individualName/gco:CharacterString";
    private static final String ORGANISATION_NAME = "gmd:organisationName/gco:CharacterString";
    private static final String EMAIL = "gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString";
    private static final String ROLE = "gmd:role/*/@codeListValue";
    private static final String DELIVERY_POINT = "gmd:contactInfo/*/gmd:address/*/gmd:deliveryPoint/gco:CharacterString";
    private static final String CITY = "gmd:contactInfo/*/gmd:address/*/gmd:city/gco:CharacterString";
    private static final String ADMINISTRATIVE_AREA = "gmd:contactInfo/*/gmd:address/*/gmd:administrativeArea/gco:CharacterString";
    private static final String POSTAL_CODE = "gmd:contactInfo/*/gmd:address/*/gmd:postalCode/gco:CharacterString";
    private static final String COUNTRY = "gmd:contactInfo/*/gmd:address/*/gmd:country/gco:CharacterString";
    private final XPathExpression responsibleParties, individualName, organisationName, email, role,
        deliveryPoint, city, administrativeArea, postalCode, country;

    public ResponsiblePartyConverter(XPath xpath, String ResponsiblePartyXpath) throws XPathExpressionException {
        this.responsibleParties = xpath.compile(checkNotNull(ResponsiblePartyXpath));
        this.individualName = xpath.compile(INDIVIDUAL_NAME);
        this.organisationName = xpath.compile(ORGANISATION_NAME);
        this.email = xpath.compile(EMAIL);
        this.role = xpath.compile(ROLE);
        this.deliveryPoint = xpath.compile(DELIVERY_POINT);
        this.city = xpath.compile(CITY);
        this.administrativeArea = xpath.compile(ADMINISTRATIVE_AREA);
        this.postalCode = xpath.compile(POSTAL_CODE);
        this.country = xpath.compile(COUNTRY);
    }
    
    public List<ResponsibleParty> convert(Document document) throws XPathExpressionException {
        List<ResponsibleParty> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) responsibleParties.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            ResponsibleParty responsibleParty = ResponsibleParty.builder()
                .individualName(individualName.evaluate(node))
                .organisationName(organisationName.evaluate(node))
                .email(email.evaluate(node))
                .role(role.evaluate(node))
                .address(convertAddress(node))
                .build();
            toReturn.add(responsibleParty);
        }
        return toReturn;
    }
    
    private Address convertAddress(Node responsiblePartyNode) throws XPathExpressionException {
        return Address.builder()
            .deliveryPoint(deliveryPoint.evaluate(responsiblePartyNode))
            .city(city.evaluate(responsiblePartyNode))
            .administrativeArea(administrativeArea.evaluate(responsiblePartyNode))
            .postalCode(postalCode.evaluate(responsiblePartyNode))
            .country(country.evaluate(responsiblePartyNode))
            .build();
    }
}
