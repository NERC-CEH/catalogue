package uk.ac.ceh.ukeof.model.simple.adapters;

import java.io.*;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.*;
import org.slf4j.*;

/*
 * see http://blog.bdoughan.com/2011/04/xmlanyelement-and-non-dom-properties.html
 */
public class ExtendedMetadataHandler implements DomHandler<String, StreamResult> {
    private final Logger logger = LoggerFactory.getLogger(ExtendedMetadataHandler.class);
    private static final String START_TAG = "<ukeof:extendedMetadata xmlns:ukeof=\"http://www.ukeof.org.uk/schema/1\">";
    private static final String END_TAG = "</ukeof:extendedMetadata>";
    
    private StringWriter xmlWriter = new StringWriter();

    @Override
    public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
        return new StreamResult(xmlWriter);
    }

    @Override
    public String getElement(StreamResult rt) {
        String xml = rt.getWriter().toString();
        logger.debug("\n--StreamResult--\n{}\n--END--", xml);
        int beginIndex = xml.indexOf(START_TAG) + START_TAG.length();
        logger.debug("start index: {}", xml.indexOf(START_TAG));
        int endIndex = xml.indexOf(END_TAG);
        String toReturn = xml.substring(beginIndex, endIndex);
        logger.debug("\n--substring--\n{}\n--END--", toReturn);
        return toReturn;
    }

    @Override
    public Source marshal(String n, ValidationEventHandler errorHandler) {
        try {
            String xml = START_TAG + n.trim() + END_TAG;
            logger.debug("\n--MARSHAL--\nn: {} \nxml: {}\n--END--", n, xml);
            StringReader xmlReader = new StringReader(n);
            return new StreamSource(xmlReader);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}