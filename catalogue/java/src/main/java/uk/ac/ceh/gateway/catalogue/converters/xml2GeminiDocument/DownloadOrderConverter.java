package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import uk.ac.ceh.gateway.catalogue.gemini.DownloadOrder;

public class DownloadOrderConverter {
    private static final String ORDER_URL = "/*/gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine[*/gmd:function/*/@codeListValue=\"order\"]/*/gmd:linkage/gmd:URL[contains(text(), \"://gateway.ceh.ac.uk/download?fileIdentifier=\") or contains(text(), \"://catalogue.ceh.ac.uk/download?fileIdentifier=\")]";
    private static final String SUPPORTING_DOCUMENTS_URL = "/*/gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine[*/gmd:function/*/@codeListValue=\"information\"]/*/gmd:linkage/gmd:URL[starts-with(text(), \"http://eidchub.ceh.ac.uk/metadata\")]";
    private final XPathExpression orderUrl, supportingDocumentsUrl;
    
    public DownloadOrderConverter(XPath xpath) throws XPathExpressionException {
        this.orderUrl = xpath.compile(ORDER_URL);
        this.supportingDocumentsUrl = xpath.compile(SUPPORTING_DOCUMENTS_URL);
    }
    
    public DownloadOrder convert(Document document) throws XPathExpressionException {
        return DownloadOrder.builder()
            .orderUrl(orderUrl.evaluate(document))
            .supportingDocumentsUrl(supportingDocumentsUrl.evaluate(document))
            .build();
    }
}