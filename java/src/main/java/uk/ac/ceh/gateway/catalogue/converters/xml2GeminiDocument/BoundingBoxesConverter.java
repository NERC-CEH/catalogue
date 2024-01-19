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
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;

public class BoundingBoxesConverter {
    private static final String BOUNDING_BOXES = "/*/gmd:identificationInfo/*/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox | /*/gmd:identificationInfo/*/srv:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
    private static final String WEST_BOUNDING_LONGITUDE = "gmd:westBoundLongitude/gco:Decimal";
    private static final String EAST_BOUNDING_LONGITUDE = "gmd:eastBoundLongitude/gco:Decimal";
    private static final String SOUTH_BOUNDING_LATITUDE = "gmd:southBoundLatitude/gco:Decimal";
    private static final String NORTH_BOUNDING_LATITUDE = "gmd:northBoundLatitude/gco:Decimal";
    private final XPathExpression boundingBoxes, westBoundingLongitude, eastBoundingLongitude, southBoundLatitude, northBoundLatitude;

    public BoundingBoxesConverter(XPath xpath) throws XPathExpressionException {
        boundingBoxes = xpath.compile(BOUNDING_BOXES);
        westBoundingLongitude = xpath.compile(WEST_BOUNDING_LONGITUDE);
        eastBoundingLongitude = xpath.compile(EAST_BOUNDING_LONGITUDE);
        southBoundLatitude = xpath.compile(SOUTH_BOUNDING_LATITUDE);
        northBoundLatitude = xpath.compile(NORTH_BOUNDING_LATITUDE);
    }

    public List<BoundingBox> convert(Document document) throws XPathExpressionException {
        List<BoundingBox> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) boundingBoxes.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node boundingBoxNode = nodeList.item(i);
            BoundingBox boundingBox = BoundingBox.builder()
                .westBoundLongitude(westBoundingLongitude.evaluate(boundingBoxNode).trim())
                .eastBoundLongitude(eastBoundingLongitude.evaluate(boundingBoxNode).trim())
                .southBoundLatitude(southBoundLatitude.evaluate(boundingBoxNode).trim())
                .northBoundLatitude(northBoundLatitude.evaluate(boundingBoxNode).trim())
                .build();
            toReturn.add(boundingBox);
        }
        return toReturn;
    }

}
