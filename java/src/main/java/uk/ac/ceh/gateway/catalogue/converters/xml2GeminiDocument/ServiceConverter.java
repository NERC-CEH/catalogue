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
import uk.ac.ceh.gateway.catalogue.gemini.Service;

public class ServiceConverter {
    private static final String IDENTIFICATION_INFO = "/*/gmd:identificationInfo/srv:SV_ServiceIdentification";
    private static final String TYPE = "";
    private static final String VERSION = "";
    private static final String COUPLED_RESOURCE = "";
    private static final String IDENTIFIER = "";
    private static final String LAYER_NAME = "";
    private static final String OPERATION_NAME = "";
    private static final String PLATFORM = "";
    private static final String URL = "";
//    private final XPathExpression identificationInfo, type, version, coupledResource, identifier, layerName, operationName, platform, url;

    public ServiceConverter(XPath xpath) throws XPathExpressionException {
//        identificationInfo = xpath.compile(IDENTIFICATION_INFO);
//        type = xpath.compile(TYPE);
//        version = xpath.compile(VERSION);
//        coupledResource = xpath.compile(COUPLED_RESOURCE);
//        identifier = xpath.compile(IDENTIFIER);
//        layerName = xpath.compile(LAYER_NAME);
//        operationName = xpath.compile(OPERATION_NAME);
//        platform =xpath.compile(PLATFORM);
//        url = xpath.compile(URL);
    }

    public Service convert(Document document) throws XPathExpressionException {
//        List<ResourceMaintenance> toReturn = new ArrayList<>();
//        NodeList nodeList = (NodeList) resourceMaintenance.evaluate(document, XPathConstants.NODESET);
//        for(int i=0; i<nodeList.getLength(); i++){
//            Node node = nodeList.item(i);
//            toReturn.add(new ResourceMaintenance(frequencyOfUpdate.evaluate(node).trim(), note.evaluate(node).trim()));
//        }
        return Service.builder().build();
    }
}