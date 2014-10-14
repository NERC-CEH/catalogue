package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;
import uk.ac.ceh.gateway.catalogue.gemini.ThesaurusName;

public class DescriptiveKeywordsConverter {
    private static final String DESCRIPTIVE_KEYWORDS = "/*/gmd:identificationInfo/*/gmd:descriptiveKeywords/*";
    private static final String TYPE = "gmd:type/*/@codeListValue";
    private static final String THESAURUS_TITLE = "gmd:thesaurusName/*/gmd:title/*";
    private static final String THESAURUS_DATE = "gmd:thesaurusName/*/gmd:date/*/gmd:date/gco:Date";
    private static final String THESAURUS_DATE_TYPE_VALUE = "gmd:date/*/gmd:dateType/*/@codeListValue";
    private static final String KEYWORD_CHARACTER = "gmd:keyword/gco:CharacterString";
    private static final String KEYWORD_ANCHOR = "gmd:keyword/gmx:Anchor";
    private final XPathExpression descriptiveKeywords, type, thesaurusTitle, thesaurusDate,
        thesaurusDateTypeValue, keywordCharacter, keywordAnchor;

    public DescriptiveKeywordsConverter(XPath xpath) throws XPathExpressionException {
        this.descriptiveKeywords = xpath.compile(DESCRIPTIVE_KEYWORDS);
        this.type = xpath.compile(TYPE);
        this.thesaurusTitle = xpath.compile(THESAURUS_TITLE);
        this.thesaurusDate = xpath.compile(THESAURUS_DATE);
        this.thesaurusDateTypeValue = xpath.compile(THESAURUS_DATE_TYPE_VALUE);
        this.keywordCharacter = xpath.compile(KEYWORD_CHARACTER);
        this.keywordAnchor = xpath.compile(KEYWORD_ANCHOR);
    }
    
    public List<DescriptiveKeywords> convert(Document document) throws XPathExpressionException {
        List<DescriptiveKeywords> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) descriptiveKeywords.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            
            ThesaurusName thesaurusName = ThesaurusName.builder()
                    .title(thesaurusTitle.evaluate(node))
                    .date(LocalDateFactory.parse(thesaurusDate.evaluate(node)))
                    .dateType(thesaurusDateTypeValue.evaluate(node))
                    .build();
                    
            toReturn.add(DescriptiveKeywords.builder()
                    .keywords(getKeywordsFromDescriptiveKeywordsNode(node))
                    .type(type.evaluate(node))
                    .thesaurusName(thesaurusName)
                    .build()
            );
        }
        return toReturn;
    }
    
    private List<Keyword> getKeywordsFromDescriptiveKeywordsNode(Node node) throws XPathExpressionException{
        List<Keyword> toReturn = new ArrayList<>();
        toReturn.addAll(getKeywordsFromCharacterString(node));
        toReturn.addAll(getKeywordsURIFromDescriptiveKeywordsNode(node));
        return toReturn;
    }
    
    private List<Keyword> getKeywordsFromCharacterString(Node node) throws XPathExpressionException {
        List<String> keywords = NodeListConverter.getListOfStrings((NodeList) keywordCharacter.evaluate(node, XPathConstants.NODESET));
        return keywords.stream()
            .map((keyword) -> { return Keyword.builder().value(keyword).build(); })
            .collect(Collectors.toList());
    }
    
    private List<Keyword> getKeywordsURIFromDescriptiveKeywordsNode(Node descriptiveKeywordsNode) throws XPathExpressionException{
        List<Keyword> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) keywordAnchor.evaluate(descriptiveKeywordsNode, XPathConstants.NODESET);

        for(int i=0; i<nodeList.getLength(); i++){
            Node keywordNode = nodeList.item(i);
            toReturn.add(Keyword.builder()
                .value(keywordNode.getFirstChild().getNodeValue())
                .URI(keywordNode.getAttributes().getNamedItem("xlink:href").getNodeValue())
                .build()
            );
        }
        return toReturn;
    }  
}