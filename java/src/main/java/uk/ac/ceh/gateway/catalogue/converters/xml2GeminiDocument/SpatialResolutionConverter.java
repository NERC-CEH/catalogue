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
import uk.ac.ceh.gateway.catalogue.gemini.SpatialResolution;

public class SpatialResolutionConverter {
    private static final String SPATIAL_RESOLUTIONS = "/*/gmd:identificationInfo/*/gmd:spatialResolution/*";
    private static final String DISTANCE = "gmd:distance/*";
    private static final String UOM = "gmd:distance/*/@uom";
    private static final String EQUIVALENT_SCALE = "gmd:equivalentScale/*/gmd:denominator/*";
    private final XPathExpression spatialResolutions, distance, uom, equivalentScale;
    
    public SpatialResolutionConverter(XPath xpath) throws XPathExpressionException {
        this.spatialResolutions = xpath.compile(SPATIAL_RESOLUTIONS);
        this.distance = xpath.compile(DISTANCE);
        this.uom = xpath.compile(UOM);
        this.equivalentScale = xpath.compile(EQUIVALENT_SCALE);
    }
    
    public List<SpatialResolution> convert(Document document) throws XPathExpressionException {
        List<SpatialResolution> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) spatialResolutions.evaluate(document, XPathConstants.NODESET);
        
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            toReturn.add( 
                SpatialResolution.builder()
                    .distance(distance.evaluate(node))
                    .uom(uom.evaluate(node))
                    .equivalentScale(equivalentScale.evaluate(node))
                .build()
            );
        }
        return toReturn;
    }
}
