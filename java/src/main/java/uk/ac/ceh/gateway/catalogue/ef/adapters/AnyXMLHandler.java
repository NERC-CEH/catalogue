package uk.ac.ceh.gateway.catalogue.ef.adapters;

import java.io.*;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.*;
import org.slf4j.*;

/*
 * see http://blog.bdoughan.com/2011/04/xmlanyelement-and-non-dom-properties.html
 */
public class AnyXMLHandler implements DomHandler<String, StreamResult> {
    private final Logger logger = LoggerFactory.getLogger(AnyXMLHandler.class);
    private static final String START_TAG = "<ukeof:anyXML xmlns:ukeof=\"http://www.ukeof.org.uk/schema/1\">";
    private static final String END_TAG = "</ukeof:anyXML>";
    
    @Override
    public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
        return new StreamResult(new StringWriter());
    }

    @Override
    public String getElement(StreamResult streamResult) {
        String xml = streamResult.getWriter().toString();
        logger.debug("\n--StreamResult--\n{}\n--END--", xml);
        int beginIndex = xml.indexOf(START_TAG) + START_TAG.length();
        int endIndex = xml.indexOf(END_TAG);
        
        if (endIndex > -1) {
            return xml.substring(beginIndex, endIndex);
        } else {
            return null;
        }
    }

    @Override
    public Source marshal(String value, ValidationEventHandler errorHandler) {
        try {
            String xml = START_TAG + value.trim() + END_TAG;
            logger.debug("\n--MARSHAL--\nn: {} \nxml: {}\n--END--", value, xml);
            StringReader xmlReader = new StringReader(xml);
            return new StreamSource(xmlReader);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
