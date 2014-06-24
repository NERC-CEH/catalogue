package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResponsibleParty;

public class ResponsiblePartyConverter {
    private static final String RESPONSIBLE_PARTY = "//gmd:CI_ResponsibleParty";
    private static final String INDIVIDUAL_NAME = "gmd:individualName/gco:CharacterString";
    private static final String ORGANISATION_NAME = "gmd:organisationName/gco:CharacterString";
    private static final String EMAIL = "gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString";
    private static final String ROLE = "gmd:role/*/@codeListValue";
    private final XPathExpression responsibleParties, individualName, organisationName, email, role;

    public ResponsiblePartyConverter(XPath xpath) throws XPathExpressionException {
        this.responsibleParties = xpath.compile(RESPONSIBLE_PARTY);
        this.individualName = xpath.compile(INDIVIDUAL_NAME);
        this.organisationName = xpath.compile(ORGANISATION_NAME);
        this.email = xpath.compile(EMAIL);
        this.role = xpath.compile(ROLE);
    }
    
    public List<ResponsibleParty> convert(Document document) throws XPathExpressionException {
        List<ResponsibleParty> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) responsibleParties.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node responsiblePartyNode = nodeList.item(i);
            ResponsibleParty responsibleParty = ResponsibleParty.builder()
                .individualName(individualName.evaluate(responsiblePartyNode))
                .organisationName(organisationName.evaluate(responsiblePartyNode))
                .email(email.evaluate(responsiblePartyNode))
                .role(role.evaluate(responsiblePartyNode))
                .build();
            toReturn.add(responsibleParty);
        }
        return toReturn;
    }
}