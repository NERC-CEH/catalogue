package uk.ac.ceh.gateway.catalogue.converters;

import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.NodeListConverter;
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
import lombok.extern.slf4j.Slf4j;
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
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.ConformanceResultConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.DatasetReferenceDatesConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.DescriptiveKeywordsConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.DistributionInfoConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.DownloadOrderConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.OnlineResourceConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.ResourceIdentifierConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.ResourceMaintenanceConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.ResponsiblePartyConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.RevisionOfConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.SpatialReferenceSystemConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.SpatialResolutionConverter;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.TemporalExtentConverter;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;
import uk.ac.ceh.gateway.catalogue.gemini.XPaths;

/**
 *
 * @author jcoop, cjohn
 */
@Slf4j
public class Xml2GeminiDocumentMessageConverter extends AbstractHttpMessageConverter<GeminiDocument> {
    private final XPathExpression id, title, description, alternateTitle, topicCategories, resourceType, 
        browseGraphicUrl, coupledResource, resourceStatus, metadataDate, lineage, 
        metadataStandardName, metadataStandardVersion, supplementalInfo, 
        spatialRepresentationType, datasetLanguage, useLimitations, 
        accessConstraints, otherConstraints, securityConstraints, parentIdentifier;
    private final XPath xpath;
    private final ResourceIdentifierConverter resourceIdentifierConverter;
    private final DescriptiveKeywordsConverter descriptiveKeywordsConverter;
    private final ResponsiblePartyConverter distributorConverter;
    private final ResponsiblePartyConverter metadataPointOfContactConverter;
    private final ResponsiblePartyConverter responsiblePartyConverter;
    private final DownloadOrderConverter downloadOrderConverter;
    private final BoundingBoxesConverter boundingBoxesConverter;
    private final TemporalExtentConverter temporalExtentConverter;
    private final SpatialReferenceSystemConverter spatialReferenceSystem;
    private final DatasetReferenceDatesConverter datasetReferenceDatesConverter;
    private final OnlineResourceConverter onlineResourceConverter;
    private final DistributionInfoConverter distributionInfoConverter;
    private final ConformanceResultConverter conformanceResultConverter;
    private final SpatialResolutionConverter spatialResolutionConverter;
    private final RevisionOfConverter revisionOfConverter;
    private final ResourceMaintenanceConverter resourceMaintenaceConverter;
    
    public Xml2GeminiDocumentMessageConverter() throws XPathExpressionException {
        super(MediaType.APPLICATION_XML);
        
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new HardcodedGeminiNamespaceResolver());
        this.id = xpath.compile(XPaths.ID);
        this.title = xpath.compile(XPaths.TITLE);
        this.description = xpath.compile(XPaths.DESCRIPTION);
        this.alternateTitle = xpath.compile(XPaths.ALTERNATE_TITLE);
        this.topicCategories = xpath.compile(XPaths.TOPIC_CATEGORIES);
        this.resourceType = xpath.compile(XPaths.RESOURCE_TYPE);
        this.resourceIdentifierConverter = new ResourceIdentifierConverter(xpath);
        this.descriptiveKeywordsConverter = new DescriptiveKeywordsConverter(xpath);
        this.metadataPointOfContactConverter = new ResponsiblePartyConverter(xpath, XPaths.METADATA_POINT_OF_CONTACT);
        this.distributorConverter = new ResponsiblePartyConverter(xpath, XPaths.DISTRIBUTOR);
        this.responsiblePartyConverter = new ResponsiblePartyConverter(xpath, XPaths.RESPONSIBLE_PARTY);
        this.downloadOrderConverter = new DownloadOrderConverter(xpath);
        this.boundingBoxesConverter = new BoundingBoxesConverter(xpath);
        this.browseGraphicUrl = xpath.compile(XPaths.BROWSE_GRAPHIC_URL);
        this.temporalExtentConverter = new TemporalExtentConverter(xpath);
        this.coupledResource = xpath.compile(XPaths.COUPLED_RESOURCE);
        this.resourceStatus = xpath.compile(XPaths.RESOURCE_STATUS);
        this.spatialReferenceSystem = new SpatialReferenceSystemConverter(xpath);
        this.datasetReferenceDatesConverter = new DatasetReferenceDatesConverter(xpath);
        this.metadataDate = xpath.compile(XPaths.METADATA_DATE);
        this.onlineResourceConverter = new OnlineResourceConverter(xpath);
        this.distributionInfoConverter = new DistributionInfoConverter(xpath);
        this.lineage = xpath.compile(XPaths.LINEAGE);
        this.conformanceResultConverter = new ConformanceResultConverter(xpath);
        this.spatialResolutionConverter = new SpatialResolutionConverter(xpath);
        this.metadataStandardName = xpath.compile(XPaths.METADATA_STANDARD);
        this.metadataStandardVersion = xpath.compile(XPaths.METADATA_VERSION);
        this.supplementalInfo = xpath.compile(XPaths.SUPPLEMENTAL_INFO);
        this.spatialRepresentationType = xpath.compile(XPaths.SPATIAL_REPRESENTATION_TYPE);
        this.datasetLanguage = xpath.compile(XPaths.DATASET_LANGUAGE);
        this.useLimitations = xpath.compile(XPaths.USE_LIMITATION);
        this.accessConstraints = xpath.compile(XPaths.ACCESS_CONSTRAINT);
        this.otherConstraints = xpath.compile(XPaths.OTHER_CONSTRAINT);
        this.securityConstraints = xpath.compile(XPaths.SECURITY_CONSTRAINT);
        this.parentIdentifier = xpath.compile(XPaths.PARENT_IDENTIFIER);
        this.revisionOfConverter = new RevisionOfConverter(xpath);
        this.resourceMaintenaceConverter = new ResourceMaintenanceConverter(xpath);
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
            String identifier = id.evaluate(document);
            log.debug("Reading GeminiDocument: {}", identifier);
            toReturn.setId(identifier);
            toReturn.setTitle(title.evaluate(document));
            toReturn.setDescription(description.evaluate(document));
            toReturn.setAlternateTitles(getListOfStrings(document, alternateTitle));
            toReturn.setDatasetLanguages(getListOfStrings(document, datasetLanguage));
            toReturn.setDescriptiveKeywords(descriptiveKeywordsConverter.convert(document));
            toReturn.setTopicCategories(getListOfStrings(document, topicCategories));
            toReturn.setDownloadOrder(downloadOrderConverter.convert(document));
            toReturn.setResponsibleParties(responsiblePartyConverter.convert(document));
            toReturn.setResourceType(resourceType.evaluate(document));
            toReturn.setResourceIdentifiers(resourceIdentifierConverter.convert(document));
            toReturn.setDescriptiveKeywords(descriptiveKeywordsConverter.convert(document));
            toReturn.setTopicCategories(getListOfStrings(document, topicCategories));
            toReturn.setDownloadOrder(downloadOrderConverter.convert(document));
            toReturn.setMetadataPointsOfContact(metadataPointOfContactConverter.convert(document));
            toReturn.setDistributorContacts(distributorConverter.convert(document));
            toReturn.setBoundingBoxes(boundingBoxesConverter.convert(document));
            toReturn.setBrowseGraphicUrl(browseGraphicUrl.evaluate(document));
            toReturn.setTemporalExtent(temporalExtentConverter.convert(document));
            toReturn.setCoupledResources(getListOfStrings(document, coupledResource));
            toReturn.setResourceStatus(resourceStatus.evaluate(document));
            toReturn.setSpatialReferenceSystems(spatialReferenceSystem.convert(document));
            toReturn.setDatasetReferenceDate(datasetReferenceDatesConverter.convert(document));
            toReturn.setMetadataDate(LocalDateFactory.parse(metadataDate.evaluate(document)));
            toReturn.setOnlineResources(onlineResourceConverter.convert(document));
            toReturn.setDistributionFormats(distributionInfoConverter.convert(document));
            toReturn.setLineage(lineage.evaluate(document));
            toReturn.setConformanceResults(conformanceResultConverter.convert(document));
            toReturn.setSpatialResolutions(spatialResolutionConverter.convert(document));
            toReturn.setMetadataStandardName(metadataStandardName.evaluate(document));
            toReturn.setMetadataStandardVersion(metadataStandardVersion.evaluate(document));
            toReturn.setSupplementalInfo(supplementalInfo.evaluate(document));
            toReturn.setSpatialRepresentationTypes(getListOfStrings(document, spatialRepresentationType));
            toReturn.setUseLimitations(getListOfStrings(document, useLimitations));
            toReturn.setAccessConstraints(getListOfStrings(document, accessConstraints));
            toReturn.setOtherConstraints(getListOfStrings(document, otherConstraints));
            toReturn.setSecurityConstraints(getListOfStrings(document, securityConstraints));
            toReturn.setParentIdentifier(parentIdentifier.evaluate(document));
            toReturn.setRevisionOfIdentifier(revisionOfConverter.convert(document));
            toReturn.setResourceMaintenance(resourceMaintenaceConverter.convert(document));
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
    
    public static List<String> getListOfStrings(Document document, XPathExpression expression) throws XPathExpressionException{
        return NodeListConverter.getListOfStrings((NodeList) expression.evaluate(document, XPathConstants.NODESET));
    }
}