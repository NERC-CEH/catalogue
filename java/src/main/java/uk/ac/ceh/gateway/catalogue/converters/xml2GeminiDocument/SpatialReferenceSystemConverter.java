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
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;

public class SpatialReferenceSystemConverter {
    private static final String SPATIAL_REFERENCE_SYSTEM = "/*/gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier/*";
    private static final String SPATIAL_REFERENCE_SYSTEM_CODE = "gmd:code/*";
    private static final String SPATIAL_REFERENCE_SYSTEM_CODESPACE = "gmd:codeSpace/*";
    private final XPathExpression spatialReferenceSystem, spatialReferenceSystemCode, spatialReferenceSystemCodeSpace;
    
    public SpatialReferenceSystemConverter(XPath xpath) throws XPathExpressionException {
        this.spatialReferenceSystem = xpath.compile(SPATIAL_REFERENCE_SYSTEM);
        this.spatialReferenceSystemCode = xpath.compile(SPATIAL_REFERENCE_SYSTEM_CODE);
        this.spatialReferenceSystemCodeSpace = xpath.compile(SPATIAL_REFERENCE_SYSTEM_CODESPACE);
    }
    
    public List<SpatialReferenceSystem> convert(Document document) throws XPathExpressionException {
        List<SpatialReferenceSystem> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) spatialReferenceSystem.evaluate(document, XPathConstants.NODESET);
        
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            toReturn.add(SpatialReferenceSystem.builder()
                .code(spatialReferenceSystemCode.evaluate(node))
                .codeSpace(spatialReferenceSystemCodeSpace.evaluate(node))
                .build());
        }
        return toReturn;
    }
}
