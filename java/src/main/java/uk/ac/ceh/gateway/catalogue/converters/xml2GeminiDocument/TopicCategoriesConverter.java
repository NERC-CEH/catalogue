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
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

public class TopicCategoriesConverter {
    private static final String TOPIC_CATEGORIES = "/*/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode";
    private final XPathExpression topicCategories;
    private final CodeLookupService lookupService;

    public TopicCategoriesConverter(XPath xpath, CodeLookupService lookupService) throws XPathExpressionException {
        topicCategories = xpath.compile(TOPIC_CATEGORIES);
        this.lookupService = lookupService;
    }

    public List<Keyword> convert(Document document) throws XPathExpressionException {
        List<Keyword> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) topicCategories.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String value = node.getTextContent();
            Keyword keyword = Keyword.builder()
                .value(value)
                .URI(lookupService.lookup("topicCategory", value, "uri"))
                .build();
            toReturn.add(keyword);
        }
        return toReturn;
    }

}
