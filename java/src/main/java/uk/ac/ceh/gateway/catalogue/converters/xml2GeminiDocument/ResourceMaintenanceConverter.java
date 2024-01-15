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
import uk.ac.ceh.gateway.catalogue.gemini.ResourceMaintenance;

public class ResourceMaintenanceConverter {
    private static final String RESOURCE_MAINTENANCE = "/*/gmd:identificationInfo/*/gmd:resourceMaintenance/*";
    private static final String FREQUENCY_OF_UPDATE = "gmd:maintenanceAndUpdateFrequency/*/@codeListValue";
    private static final String NOTE = "gmd:maintenanceNote/*";
    private final XPathExpression resourceMaintenance, frequencyOfUpdate, note;

    public ResourceMaintenanceConverter(XPath xpath) throws XPathExpressionException {
        resourceMaintenance = xpath.compile(RESOURCE_MAINTENANCE);
        frequencyOfUpdate = xpath.compile(FREQUENCY_OF_UPDATE);
        note = xpath.compile(NOTE);
    }

    public List<ResourceMaintenance> convert(Document document) throws XPathExpressionException {
        List<ResourceMaintenance> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) resourceMaintenance.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            toReturn.add(ResourceMaintenance.builder()
                .frequencyOfUpdate(frequencyOfUpdate.evaluate(node).trim())
                .note(note.evaluate(node).trim())
                .build());
        }
        return toReturn;
    }
}
