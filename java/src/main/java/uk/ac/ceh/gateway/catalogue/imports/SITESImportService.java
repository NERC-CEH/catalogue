package uk.ac.ceh.gateway.catalogue.imports;

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

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@Profile("elter")
@Slf4j
@Service
@ToString
public class SITESImportService implements CatalogueImportService {
    // constructor prep
    private final DocumentRepository documentRepository;
    private final DocumentBuilder documentBuilder;
    private final XPath xPath;

    // constructor
    public SITESImportService(
            DocumentRepository documentRepository
    ) throws ParserConfigurationException {
        log.info("Creating");
        this.documentRepository = documentRepository;
        this.xPath = XPathFactory.newInstance().newXPath();
        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    // methods start here
    public List<String> getRemoteRecordList() {
        String SITESSitemapURL = "https://meta.fieldsites.se/sitemap.xml";
        String SITESRecordExpression = "/urlset/url/loc";
        List<String> results = new ArrayList<String>();

        log.debug("GET SITES sitemap at {}", SITESSitemapURL);

        try {
            XPathExpression SITESRecordXPath = xPath.compile(SITESRecordExpression);
            Document xmlSitemap = documentBuilder.parse(SITESSitemapURL);
            NodeList recordList = (NodeList) SITESRecordXPath.evaluate(xmlSitemap, XPathConstants.NODESET);
            int numRecords = recordList.getLength();
            for (int i=0; i<numRecords; i++){
                results.add(recordList.item(i).getTextContent());
            }
        } catch (SAXException | IOException ex) {
            log.error("Error retrieving sitemap");
        } catch (XPathExpressionException ex) {
            log.error("FEED ME A STRAY CAT");
        }
        return results;
    }

    // dummy
    public String getFullRemoteRecord(String recordID){
        return "hello";
    }

    // dummy
    public String createRecord(String recordID){
        return "hello";
    }

    // dummy
    public String updateRecord(String recordID){
        return "hello";
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport() {
        // get sitemap
        List<String> test = getRemoteRecordList();
        for (int i=0; i<5; i++){
            log.info(test.get(i));
        }
    }
}
