package uk.ac.ceh.gateway.catalogue.converters.ukeofXml2EfDocument;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.ceh.gateway.catalogue.ef.Geometry;

public class GeometryConverter {
    private static final String GEOMETRY = "/*/ukeof:geometry";
    
    private final XPathExpression geometry;
    
    public GeometryConverter(XPath xpath) throws XPathExpressionException {
        this.geometry = xpath.compile(GEOMETRY);
    }
    
    public Geometry convert(Document document) throws XPathExpressionException {
        Node geometryNode = (Node) geometry.evaluate(document, XPathConstants.NODE);
        if(geometryNode != null){
            return Geometry.builder()
                    .wkt(geometryNode.getTextContent())
                    .build();
        }
        return null;
    }
}
