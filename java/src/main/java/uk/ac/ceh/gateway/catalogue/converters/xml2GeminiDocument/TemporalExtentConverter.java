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
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;

public class TemporalExtentConverter {
    private static final String TEMPORAL_EXTENTS = "/*/gmd:identificationInfo/*/gmd:extent/*/gmd:temporalElement/*/gmd:extent/gml:TimePeriod";
    private static final String BEGIN = "gml:beginPosition";
    private static final String END = "gml:endPosition";
    private final XPathExpression temporalExtents, begin, end;

    public TemporalExtentConverter(XPath xpath) throws XPathExpressionException {
        this.temporalExtents = xpath.compile(TEMPORAL_EXTENTS);
        this.begin = xpath.compile(BEGIN);
        this.end = xpath.compile(END);
    }

    public List<TimePeriod> convert(Document document) throws XPathExpressionException {
        List<TimePeriod> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) temporalExtents.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node timePeriodNode = nodeList.item(i);
            toReturn.add(TimePeriod.builder().begin(begin.evaluate(timePeriodNode)).end(end.evaluate(timePeriodNode)).build());
        }
        return toReturn;
    }
}
