package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;

/**
 * The following DataciteIndexingService detects when a GeminiDocument has been
 * updated and submits an update to the datacite rest api if it has been determined
 * that the change in the GeminiDocument adjusts the datacite metadata request.
 * @author cjohn
 */
@Data
@Slf4j
public class DataciteIndexingService implements DocumentIndexingService {
    private final BundledReaderService<MetadataDocument> bundleReader;
    private final DataciteService datacite;
    
    private final XPathExpression dateSubmittedXPath;
    
    public DataciteIndexingService(BundledReaderService<MetadataDocument> bundleReader, DataciteService datacite) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        this.dateSubmittedXPath = xpath.compile("/*/dates/date[@dateType='Submitted']");
        this.bundleReader = bundleReader;
        this.datacite = datacite;
    }

    /**
     * Loop around all of the documents ids in the toIndex. Read these and if a
     * GeminiDocument was detected then submit this to #indexDocument(GeminiDocument)
     * @param toIndex list of ids to index
     * @param revision the revision to index at
     */
    @Override
    public void indexDocuments(List<String> toIndex, String revision) {
        for(String metadataId: toIndex) {
            try {
                MetadataDocument document = bundleReader.readBundle(metadataId, revision);
                if (document instanceof GeminiDocument) {
                    indexDocument((GeminiDocument)document);
                }
            }
            catch(Exception ex) {
                log.error("Failed to read metadata document", ex);
            }
        }
    }
    
    /**
     * Generate a new datacite metadata request based upon the new GeminiDocument
     * If this request differs to the one which datacite already have then we can
     * submit an update.
     * @param document to check if updates are required
     * @throws Exception if any problems occur when trying to index
     */
    public void indexDocument(GeminiDocument document) throws Exception {
        if(datacite.isDatacited(document)) {
            String lastRequest = datacite.getDoiMetadata(document); //Get the latest request
            LocalDate submittedDate = getDateSubmitted(lastRequest);
            
            String newRequest = datacite.getDatacitationRequest(document, submittedDate);
            if(!newRequest.equals(lastRequest)) {
                log.info("Submitting datacite update: {}", document.getId());
                datacite.updateDoiMetadata(document);
            }
        }
    }
    
    /**
     * Return the date submitted date specified in the latestRequest
     * @param latestRequest xml string containing a datacite request
     * @return the date submitted in the xml
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException 
     */
    public LocalDate getDateSubmitted(String latestRequest) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException  {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document xmlRequest = factory.newDocumentBuilder().parse(new InputSource(new StringReader(latestRequest)));
        String dateSubmitted = dateSubmittedXPath.evaluate(xmlRequest);
        
        return LocalDate.parse(dateSubmitted);
    }
    
    /**
     * Datacitation repository is assumed to never be empty
     * @return false
     * @throws DocumentIndexingException 
     */
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return false;
    }

    // Do nothing here
    @Override
    public void rebuildIndex() throws DocumentIndexingException {}

    // Do nothing here
    @Override
    public void unindexDocuments(List<String> unIndex) throws DocumentIndexingException {}
}
