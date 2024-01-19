package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.time.LocalDate;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.LocalDateFactory;

public class DatasetReferenceDatesConverter {
    private static final String CREATION = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:date[*/gmd:dateType/*/@codeListValue='creation']";
    private static final String PUBLICATION = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:date[*/gmd:dateType/*/@codeListValue='publication']";
    private static final String REVISION = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:date[*/gmd:dateType/*/@codeListValue='revision']";
    private static final String DATE = "*/gmd:date/gco:Date";
    private final XPathExpression creation, publication, revision, date;

    public DatasetReferenceDatesConverter(XPath xpath) throws XPathExpressionException{
        this.creation = xpath.compile(CREATION);
        this.publication = xpath.compile(PUBLICATION);
        this.revision = xpath.compile(REVISION);
        this.date = xpath.compile(DATE);
    }

    public DatasetReferenceDate convert(Document document) throws XPathExpressionException{
        return DatasetReferenceDate.builder()
                .creationDate(getDate(this.creation, document))
                .publicationDate(getDate(this.publication, document))
                .revisionDate(getDate(this.revision, document))
                .build();
    }

    private LocalDate getDate(XPathExpression dateExpression, Document document) throws XPathExpressionException{
        LocalDate toReturn = null;
        Node dateNode = (Node) dateExpression.evaluate(document, XPathConstants.NODE);
        if(dateNode != null){
            toReturn = LocalDateFactory.parse(this.date.evaluate(dateNode));
        }
        return toReturn;
    }
}
