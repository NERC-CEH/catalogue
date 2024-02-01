package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RevisionOfConverter {
    private static final String REVISION_OF = "/*/gmd:identificationInfo/*/gmd:aggregationInfo/*[gmd:associationType/*/@codeListValue = 'revisionOf']/gmd:aggregateDataSetIdentifier/*";
    private static final String CODE = "gmd:code/*";
    private static final String CODESPACE = "gmd:codeSpace/*";
    private final XPathExpression revisionOf, code, codeSpace;

    public RevisionOfConverter(XPath xpath) throws XPathExpressionException {
        revisionOf = xpath.compile(REVISION_OF);
        code = xpath.compile(CODE);
        codeSpace = xpath.compile(CODESPACE);
    }

    public String convert(Document document) throws XPathExpressionException {
        NodeList nodeList = (NodeList) revisionOf.evaluate(document, XPathConstants.NODESET);

        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return String.format("%s#%s", codeSpace.evaluate(node), code.evaluate(node));
        } else {
            return null;
        }
    }
}
