package uk.ac.ceh.ukeof.model.simple.adapters;

import java.io.*;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.*;

/*
 * see http://blog.bdoughan.com/2011/04/xmlanyelement-and-non-dom-properties.html
 */
public class ExtendedMetadataHandler implements DomHandler<String, StreamResult> {
    
    private static final String START_TAG = "<ukeof:extendedMetadata>";
    private static final String END_TAG = "</ukeof:extendedMetadata>";
    
    private StringWriter xmlWriter = new StringWriter();

    @Override
    public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
        return new StreamResult(xmlWriter);
    }

    @Override
    public String getElement(StreamResult rt) {
        String xml = rt.getWriter().toString();
        int beginIndex = xml.indexOf(START_TAG) + START_TAG.length();
        int endIndex = xml.indexOf(END_TAG);
        return xml.substring(beginIndex, endIndex);
    }

    @Override
    public Source marshal(String n, ValidationEventHandler errorHandler) {
        try {
            String xml = START_TAG + n.trim() + END_TAG;
            StringReader xmlReader = new StringReader(xml);
            return new StreamSource(xmlReader);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}