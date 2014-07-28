package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.BoundingBoxesConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.DescriptiveKeywordsConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.DownloadOrderConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.DatasetReferenceDatesConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.NodeListConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.ResourceIdentifierConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.ResponsiblePartyConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.SpatialReferenceSystemConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.TemporalExtentConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.common.DateHandler;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.XPaths;

/**
 *
 * @author jcoop, cjohn
 */
public class Xml2GeminiDocumentMessageConverter extends AbstractHttpMessageConverter<GeminiDocument> {
    private final XPathExpression id, title, description, alternateTitle, 
            languageCodeList, languageCodeListValue, topicCategories, 
            resourceTypeCodeList, resourceTypeCodeListValue, 
            otherCitationDetails, browseGraphicUrl, coupledResource,
            resourceStatus, metadataDate;
    private final XPath xpath;
    private final ResourceIdentifierConverter resourceIdentifierConverter;
    private final DescriptiveKeywordsConverter descriptiveKeywordsConverter;
    private final ResponsiblePartyConverter responsiblePartyConverter;
    private final DownloadOrderConverter downloadOrderConverter;
    private final BoundingBoxesConverter boundingBoxesConverter;
    private final TemporalExtentConverter temporalExtentConverter;
    private final SpatialReferenceSystemConverter spatialReferenceSystem;
    private final DatasetReferenceDatesConverter datasetReferenceDatesConverter;
    
    public Xml2GeminiDocumentMessageConverter() throws XPathExpressionException {
        super(MediaType.APPLICATION_XML);
        
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new HardcodedNamespaceResolver());
        this.id = xpath.compile(XPaths.ID);
        this.title = xpath.compile(XPaths.TITLE);
        this.description = xpath.compile(XPaths.DESCRIPTION);
        this.alternateTitle = xpath.compile(XPaths.ALTERNATE_TITLE);
        this.languageCodeList = xpath.compile(XPaths.LANGUAGE_CODE_LIST);
        this.languageCodeListValue = xpath.compile(XPaths.LANGUAGE_CODE_LIST_VALUE);
        this.topicCategories = xpath.compile(XPaths.TOPIC_CATEGORIES);
        this.otherCitationDetails = xpath.compile(XPaths.OTHER_CITATION_DETAILS);
        this.resourceTypeCodeList = xpath.compile(XPaths.RESOURCE_TYPE_CODE_LIST);
        this.resourceTypeCodeListValue = xpath.compile(XPaths.RESOURCE_TYPE_CODE_LIST_VALUE);
        this.resourceIdentifierConverter = new ResourceIdentifierConverter(xpath);
        this.descriptiveKeywordsConverter = new DescriptiveKeywordsConverter(xpath);
        this.responsiblePartyConverter = new ResponsiblePartyConverter(xpath);
        this.downloadOrderConverter = new DownloadOrderConverter(xpath);
        this.boundingBoxesConverter = new BoundingBoxesConverter(xpath);
        this.browseGraphicUrl = xpath.compile(XPaths.BROWSE_GRAPHIC_URL);
        this.temporalExtentConverter = new TemporalExtentConverter(xpath);
        this.coupledResource = xpath.compile(XPaths.COUPLED_RESOURCE);
        this.resourceStatus = xpath.compile(XPaths.RESOURCE_STATUS);
        this.spatialReferenceSystem = new SpatialReferenceSystemConverter(xpath);
        this.datasetReferenceDatesConverter = new DatasetReferenceDatesConverter(xpath);
        this.metadataDate = xpath.compile(XPaths.METADATA_DATE);
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
            toReturn.setDescription(description.evaluate(document));
            toReturn.setAlternateTitles(getListOfStrings(document, alternateTitle));
            toReturn.setDatasetLanguage(CodeListItem
                    .builder()
                    .codeList(languageCodeList.evaluate(document))
                    .value(languageCodeListValue.evaluate(document))
                    .build()
            );
            toReturn.setDescriptiveKeywords(descriptiveKeywordsConverter.convert(document));
            toReturn.setTopicCategories(getListOfStrings(document, topicCategories));
            toReturn.setDownloadOrder(downloadOrderConverter.convert(document));
            toReturn.setOtherCitationDetails(otherCitationDetails.evaluate(document));
            toReturn.setResponsibleParties(responsiblePartyConverter.convert(document));
            toReturn.setResourceType(CodeListItem
                    .builder()
                    .codeList(resourceTypeCodeList.evaluate(document))
                    .value(resourceTypeCodeListValue.evaluate(document))
                    .build()
            );
            toReturn.setResourceIdentifiers(resourceIdentifierConverter.convert(document));
            toReturn.setDescriptiveKeywords(descriptiveKeywordsConverter.convert(document));
            toReturn.setTopicCategories(getListOfStrings(document, topicCategories));
            toReturn.setDownloadOrder(downloadOrderConverter.convert(document));
            toReturn.setOtherCitationDetails(otherCitationDetails.evaluate(document));
            toReturn.setResponsibleParties(responsiblePartyConverter.convert(document));
            toReturn.setBoundingBoxes(boundingBoxesConverter.convert(document));
            toReturn.setBrowseGraphicUrl(browseGraphicUrl.evaluate(document));
            toReturn.setTemporalExtent(temporalExtentConverter.convert(document));
            toReturn.setCoupleResources(getListOfStrings(document, coupledResource));
            toReturn.setResourceStatus(resourceStatus.evaluate(document));
            toReturn.setSpatialReferenceSystem(spatialReferenceSystem.convert(document));
            toReturn.setDatasetReferenceDate(datasetReferenceDatesConverter.convert(document));
            toReturn.setMetadataDate(DateHandler.parseEmptyString(metadataDate.evaluate(document)));
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
    
    private List<String> getListOfStrings(Document document, XPathExpression expression) throws XPathExpressionException{
        return NodeListConverter.getListOfStrings((NodeList) expression.evaluate(document, XPathConstants.NODESET));
    }   
}