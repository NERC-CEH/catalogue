package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.joda.time.LocalDate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.elements.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ThesaurusName;

public class DescriptiveKeywordsConverter {
    private static final String DESCRIPTIVE_KEYWORDS = "/*/gmd:identificationInfo/*/gmd:descriptiveKeywords";
    private static final String CODE_LIST = "*/gmd:type/gmd:MD_KeywordTypeCode/@codeList";
    private static final String CODE_VALUE = "*/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue";
    private static final String THESAURUS_TITLE = "*/gmd:thesaurusName/*/gmd:title/gco:CharacterString";
    private static final String THESAURUS_DATE = "*/gmd:thesaurusName/*/gmd:date/*/gmd:date/gco:Date";
    private static final String THESAURUS_DATE_TYPE_LIST = "*/gmd:date/*/gmd:dateType/*/@codeList";
    private static final String THESAURUS_DATE_TYPE_VALUE = "*/gmd:date/*/gmd:dateType/*/@codeListValue";
    private static final String KEYWORD_CHARACTER = "*/gmd:keyword/gco:CharacterString";
    private static final String KEYWORD_ANCHOR = "*/gmd:keyword/gmx:Anchor";
    private final XPathExpression descriptiveKeywords, codeList, codeValue, thesaurusTitle, thesaurusDate,
        thesaurusDateTypeList, thesaurusDateTypeValue, keywordCharacter, keywordAnchor;

    public DescriptiveKeywordsConverter(XPath xpath) throws XPathExpressionException {
        this.descriptiveKeywords = xpath.compile(DESCRIPTIVE_KEYWORDS);
        this.codeList = xpath.compile(CODE_LIST);
        this.codeValue = xpath.compile(CODE_VALUE);
        this.thesaurusTitle = xpath.compile(THESAURUS_TITLE);
        this.thesaurusDate = xpath.compile(THESAURUS_DATE);
        this.thesaurusDateTypeList = xpath.compile(THESAURUS_DATE_TYPE_LIST);
        this.thesaurusDateTypeValue = xpath.compile(THESAURUS_DATE_TYPE_VALUE);
        this.keywordCharacter = xpath.compile(KEYWORD_CHARACTER);
        this.keywordAnchor = xpath.compile(KEYWORD_ANCHOR);
    }
    
    public List<DescriptiveKeywords> convert(Document document) throws XPathExpressionException {
        List<DescriptiveKeywords> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) descriptiveKeywords.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node descriptiveKeywordsNode = nodeList.item(i);
            
            List<Keyword> keywords = getKeywordsFromDescriptiveKeywordsNode(descriptiveKeywordsNode);
            
            CodeListItem type = CodeListItem.builder()
                    .codeList(codeList.evaluate(descriptiveKeywordsNode))
                    .value(codeValue.evaluate(descriptiveKeywordsNode))
                    .build();
            
            ThesaurusName thesaurusName = ThesaurusName
                    .builder()
                    .title(thesaurusTitle.evaluate(descriptiveKeywordsNode))
                    .date(getDate(thesaurusDate.evaluate(descriptiveKeywordsNode)))
                    .dateType(CodeListItem.builder()
                            .codeList(thesaurusDateTypeList.evaluate(descriptiveKeywordsNode))
                            .value(thesaurusDateTypeValue.evaluate(descriptiveKeywordsNode))
                            .build())
                    .build();
                    
            toReturn.add(DescriptiveKeywords.builder()
                    .keywords(keywords)
                    .type(type)
                    .thesaurusName(thesaurusName)
                    .build()
            );
        }
        return toReturn;
    }
    
    private LocalDate getDate(String nodeValue){
        LocalDate toReturn = null;
        if(nodeValue != null && !nodeValue.isEmpty()){
            String[] dateParts = nodeValue.split("-");
            if(dateParts.length == 3){
                toReturn = new LocalDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));
            }else{
                throw new IllegalArgumentException(String.format("Unable to parse date.  Expected a date format of 'yyyy-mm-dd', eg 2014-06-03, but found this: %s.", nodeValue));
            }
        }
        return toReturn;
    }
    
    private List<Keyword> getKeywordsFromDescriptiveKeywordsNode(Node descriptiveKeywordsNode) throws XPathExpressionException{
        List<Keyword> toReturn = new ArrayList<>();
        List<Keyword> keywordsWithoutURI = getKeywordsFromCharacterString(descriptiveKeywordsNode);
        List<Keyword> keywordsWithURI = getKeywordsURIFromDescriptiveKeywordsNode(descriptiveKeywordsNode);
        toReturn.addAll(keywordsWithoutURI);
        toReturn.addAll(keywordsWithURI);
        return toReturn;
    }
    
    private List<Keyword> getKeywordsFromCharacterString(Node descriptiveKeywordsNode) throws XPathExpressionException {
        List<Keyword> toReturn = new ArrayList<>();
        List<String> keywords = getListOfStrings((NodeList) keywordCharacter.evaluate(descriptiveKeywordsNode, XPathConstants.NODESET));
        if(keywords != null && !keywords.isEmpty()){
            keywords.stream().forEach((keyword) -> {
                toReturn.add(Keyword.builder()
                    .value(keyword)
                    .URI(null)
                    .build()
                );
            });
        }
        return toReturn;
    }
    
    private List<Keyword> getKeywordsURIFromDescriptiveKeywordsNode(Node descriptiveKeywordsNode) throws XPathExpressionException{
        List<Keyword> toReturn = new ArrayList<>();
        NodeList xlinkKeywords = (NodeList) keywordAnchor.evaluate(descriptiveKeywordsNode, XPathConstants.NODESET);
        if(xlinkKeywords != null && xlinkKeywords.getLength() > 0){
            for(int i=0; i<xlinkKeywords.getLength(); i++){
                Node xlinkKeyword = xlinkKeywords.item(i);
                toReturn.add(Keyword.builder()
                        .value(xlinkKeyword.getFirstChild().getNodeValue())
                        .URI(xlinkKeyword.getAttributes().getNamedItem("xlink:href").getNodeValue())
                        .build()
                );
            }
        }
        return toReturn;
    }
    
    private List<String> getListOfStrings(NodeList nodeList) throws XPathExpressionException{
        ArrayList<String> toReturn = new ArrayList<>();
        for(int i=0; i<nodeList.getLength(); i++){
            toReturn.add(nodeList.item(i).getFirstChild().getNodeValue());
        }
        return toReturn;
    }   
}