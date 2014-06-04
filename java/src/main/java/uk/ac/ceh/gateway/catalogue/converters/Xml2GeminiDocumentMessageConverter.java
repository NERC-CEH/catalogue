package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListValue;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.elements.XPaths;

/**
 *
 * @author jcoop, cjohn
 */
public class Xml2GeminiDocumentMessageConverter extends AbstractHttpMessageConverter<GeminiDocument> {
    private final XPathExpression id, title, alternateTitle, languageCodeList, languageCodeListValue, 
            topicCategories, descriptiveKeywords;
    private final XPath xpath;
    
    public Xml2GeminiDocumentMessageConverter() throws XPathExpressionException {
        super(MediaType.APPLICATION_XML);
        
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new HardcodedNamespaceResolver());
        this.id = xpath.compile(XPaths.ID);
        this.title = xpath.compile(XPaths.TITLE);
        this.alternateTitle = xpath.compile(XPaths.ALTERNATE_TITLE);
        this.languageCodeList = xpath.compile(XPaths.LANGUAGE_CODE_LIST);
        this.languageCodeListValue = xpath.compile(XPaths.LANGUAGE_CODE_LIST_VALUE);
        this.topicCategories = xpath.compile(XPaths.TOPIC_CATEGORIES);
        this.descriptiveKeywords = xpath.compile(XPaths.DESCRIPTIVE_KEYWORDS);
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(GeminiDocument.class);
    }

    @Override
    protected GeminiDocument readInternal(Class<? extends GeminiDocument> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputMessage.getBody());

            GeminiDocument toReturn = new GeminiDocument();
            toReturn.setId(id.evaluate(document));
            toReturn.setTitle(title.evaluate(document));
            toReturn.setAlternateTitles(getNodeListValuesEvaluate(document, alternateTitle));
            toReturn.setDatasetLanguage(CodeListValue
                    .builder()
                    .codeList(languageCodeList.evaluate(document))
                    .value(languageCodeListValue.evaluate(document))
                    .build()
            );
            toReturn.setDescriptiveKeywords(getDescriptiveKeywords(document, descriptiveKeywords));
            toReturn.setTopicCategories(getNodeListValuesEvaluate(document, topicCategories));
            return toReturn;
        }
        catch(ParserConfigurationException pce) {
            throw new HttpMessageNotReadableException("The document reader was not set up correctly", pce);
        } catch (SAXException se) {
            throw new HttpMessageNotReadableException("The xml content could not be parsed", se);
        } catch (XPathExpressionException ex) {
            throw new HttpMessageNotReadableException("An xpath failed to evaluate", ex);
        }
    }

    @Override
    protected void writeInternal(GeminiDocument t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("I will not be able to write that document for you");
    }
    
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false; // I can never write
    }
    
    private List<String> getNodeListValuesEvaluate(Document document, XPathExpression expression) throws XPathExpressionException{
        return getNodeListValues((NodeList) expression.evaluate(document, XPathConstants.NODESET));
    }
    
    private List<String> getNodeListValues(NodeList nodeList) throws XPathExpressionException{
        ArrayList<String> toReturn = new ArrayList<>();
        for(int i=0; i<nodeList.getLength(); i++){
            toReturn.add(nodeList.item(i).getFirstChild().getNodeValue());
        }
        return toReturn;
    }
    
    private List<DescriptiveKeywords> getDescriptiveKeywords(Document document, XPathExpression expression) throws XPathExpressionException{
        List<DescriptiveKeywords> toReturn = new ArrayList<>();
        NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        for(int i=0; i<nodeList.getLength(); i++){
            Node descriptiveKeywordsNodes = nodeList.item(i);
            
            NodeList keywords = (NodeList) xpath.evaluate("*/gmd:keyword/gco:CharacterString", descriptiveKeywordsNodes, XPathConstants.NODESET);
            CodeListValue
                    .builder()
                    .codeList(xpath.evaluate("*/gmd:type/gmdMD_KeywordTypeCode/@codeList", descriptiveKeywordsNodes))
                    .value(xpath.evaluate("*/gmd:type/gmdMD_KeywordTypeCode/@codeListValue", descriptiveKeywordsNodes))
                    .build();
            toReturn.add(DescriptiveKeywords
                    .builder()
                    .keywords(getNodeListValues(keywords))
                    .build()
            );
            
            
        }
        return toReturn;
    }

}
