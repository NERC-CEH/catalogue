package uk.ac.ceh.gateway.catalogue.converters.ukeofXml2EfDocument;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;

public class BoundingBoxConverter {
    private static final String BOUNDING_BOXES = "/*/ukeof:boundingBox";
    private static final String WEST_BOUNDING_LONGITUDE = "ukeof:westBoundLongitude";
    private static final String EAST_BOUNDING_LONGITUDE = "ukeof:eastBoundLongitude";
    private static final String SOUTH_BOUNDING_LATITUDE = "ukeof:southBoundLatitude";
    private static final String NORTH_BOUNDING_LATITUDE = "ukeof:northBoundLatitude";
    private final XPathExpression boundingBox, westBoundingLongitude, eastBoundingLongitude, southBoundLatitude, northBoundLatitude;

    public BoundingBoxConverter(XPath xpath) throws XPathExpressionException {
        boundingBox = xpath.compile(BOUNDING_BOXES);
        westBoundingLongitude = xpath.compile(WEST_BOUNDING_LONGITUDE);
        eastBoundingLongitude = xpath.compile(EAST_BOUNDING_LONGITUDE);
        southBoundLatitude = xpath.compile(SOUTH_BOUNDING_LATITUDE);
        northBoundLatitude = xpath.compile(NORTH_BOUNDING_LATITUDE);
    }

    public BoundingBox convert(Document document) throws XPathExpressionException {
        Node boundingBoxNode = (Node) boundingBox.evaluate(document, XPathConstants.NODE);
        if(boundingBoxNode != null){
            return BoundingBox.builder()
                .westBoundLongitude(westBoundingLongitude.evaluate(boundingBoxNode).trim())
                .eastBoundLongitude(eastBoundingLongitude.evaluate(boundingBoxNode).trim())
                .southBoundLatitude(southBoundLatitude.evaluate(boundingBoxNode).trim())
                .northBoundLatitude(northBoundLatitude.evaluate(boundingBoxNode).trim())
                .build();
        }
        return null;
    }
}