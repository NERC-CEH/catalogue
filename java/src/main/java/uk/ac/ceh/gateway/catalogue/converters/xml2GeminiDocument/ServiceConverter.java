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
import uk.ac.ceh.gateway.catalogue.gemini.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Service.CoupledResource;
import uk.ac.ceh.gateway.catalogue.gemini.Service.OperationMetadata;

public class ServiceConverter {
    private static final String IDENTIFICATION_INFO = "/*/gmd:identificationInfo/srv:SV_ServiceIdentification";
    private static final String TYPE = "srv:serviceType/gco:LocalName";
    private static final String COUPLING_TYPE = "srv:couplingType/*/@codeListValue";
    private static final String VERSIONS = "srv:serviceTypeVersion/*";
    private static final String COUPLED_RESOURCES = "srv:coupledResource/*";
    private static final String IDENTIFIER = "srv:identifier/*";
    private static final String LAYER_NAME = "gco:ScopedName/text()";
    private static final String OPERATION_NAME = "srv:operationName/*";
    private static final String CONTAINS_OPERATIONS = "srv:containsOperations/*";
    private static final String PLATFORMS = "srv:DCP/srv:DCPList/@codeListValue";
    private static final String URLS = "srv:connectPoint/*/gmd:linkage/gmd:URL";
    private final XPathExpression identificationInfo, type, couplingType, versions, coupledResources, operationName,
        identifier, layerName, containsOperations, platforms, urls;

    public ServiceConverter(XPath xpath) throws XPathExpressionException {
        identificationInfo = xpath.compile(IDENTIFICATION_INFO);
        type = xpath.compile(TYPE);
        couplingType = xpath.compile(COUPLING_TYPE);
        versions = xpath.compile(VERSIONS);
        coupledResources = xpath.compile(COUPLED_RESOURCES);
        operationName = xpath.compile(OPERATION_NAME);
        identifier = xpath.compile(IDENTIFIER);
        layerName = xpath.compile(LAYER_NAME);
        containsOperations =xpath.compile(CONTAINS_OPERATIONS);
        platforms =xpath.compile(PLATFORMS);
        urls = xpath.compile(URLS);
    }

    public Service convert(Document document) throws XPathExpressionException {
        Node service = (Node) identificationInfo.evaluate(document, XPathConstants.NODE);
        
        if (service == null) {
            return null;
        }
        
        return Service.builder()
            .type(type.evaluate(service))
            .couplingType(couplingType.evaluate(service))
            .versions(NodeListConverter.getListOfStrings((NodeList) versions.evaluate(service, XPathConstants.NODESET)))
            .coupledResources(getCoupledResources(service))
            .containsOperations(getOperationMetadata(service))
            .build();
    }
    
    private List<CoupledResource> getCoupledResources(Node service) throws XPathExpressionException {
        List<CoupledResource> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) coupledResources.evaluate(service, XPathConstants.NODESET);
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            CoupledResource resource = CoupledResource.builder()
                .operationName(operationName.evaluate(node))
                .identifier(identifier.evaluate(node))
                .layerName(layerName.evaluate(node))
                .build();
            toReturn.add(resource);
        }
        return toReturn;
    }
    
    private List<OperationMetadata> getOperationMetadata(Node service) throws XPathExpressionException {
        List<OperationMetadata> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) containsOperations.evaluate(service, XPathConstants.NODESET);
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            OperationMetadata operationMetadata = OperationMetadata.builder()
                .operationName(operationName.evaluate(node))
                .platforms(getPlatforms(node))
                .urls(getUrls(node))
                .build();
            toReturn.add(operationMetadata);
        }
        return toReturn;
    }
    
    private List<String> getPlatforms(Node operationMetadata) throws XPathExpressionException {
        return NodeListConverter.getListOfStrings((NodeList) platforms.evaluate(operationMetadata, XPathConstants.NODESET));
    }

    private List<String> getUrls(Node operationMetadata) throws XPathExpressionException {
        return NodeListConverter.getListOfStrings((NodeList) urls.evaluate(operationMetadata, XPathConstants.NODESET));
    }
}