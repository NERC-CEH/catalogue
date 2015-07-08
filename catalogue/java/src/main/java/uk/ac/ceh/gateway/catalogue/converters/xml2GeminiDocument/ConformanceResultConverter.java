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
import uk.ac.ceh.gateway.catalogue.gemini.ConformanceResult;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

public class ConformanceResultConverter {
    private static final String CONFORMANCE_RESULTS = "/*/gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*";
    private static final String TITLE = "gmd:specification/*/gmd:title/*";
    private static final String DATE = "gmd:specification/*/gmd:date/*/gmd:date/*";
    private static final String DATE_TYPE = "gmd:specification/*/gmd:date/*/gmd:dateType/*/@codeListValue";
    private static final String EXPLANATION = "gmd:explanation/*";
    private static final String PASS = "gmd:pass/*";
    private final XPathExpression conformanceResults, title, date, dateType, explanation, pass;
    
    public ConformanceResultConverter(XPath xpath) throws XPathExpressionException {
        this.conformanceResults = xpath.compile(CONFORMANCE_RESULTS);
        this.title = xpath.compile(TITLE);
        this.date = xpath.compile(DATE);
        this.dateType = xpath.compile(DATE_TYPE);
        this.explanation = xpath.compile(EXPLANATION);
        this.pass = xpath.compile(PASS);
    }
    
    public List<ConformanceResult> convert(Document document) throws XPathExpressionException {
        List<ConformanceResult> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) conformanceResults.evaluate(document, XPathConstants.NODESET);
        
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            toReturn.add( 
                ConformanceResult.builder()
                    .title(title.evaluate(node))
                    .date(LocalDateFactory.parse(date.evaluate(node)))
                    .dateType(dateType.evaluate(node))
                    .explanation(explanation.evaluate(node))
                    .pass(Boolean.valueOf(pass.evaluate(node)))
                .build()
            );
        }
        return toReturn;
    }
}