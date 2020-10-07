package uk.ac.ceh.gateway.catalogue.converters;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;

/**
 * The following HttpMessageConverter is responsible for transforming the
 * WmsFeatureInfo in to the xml format which a client would expect to be 
 * returned from an esri powered WMS GetFeatureInfo request (with type text/xml)
 */
@Slf4j
@ToString
public class WmsFeatureInfo2XmlMessageConverter extends AbstractHttpMessageConverter<WmsFeatureInfo> {
    public WmsFeatureInfo2XmlMessageConverter() {
        super(MediaType.TEXT_XML);
        log.info("Creating {}", this);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(WmsFeatureInfo.class);
    }

    @Override
    protected WmsFeatureInfo readInternal(Class<? extends WmsFeatureInfo> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("I can not read WmsFeatureInfo");
    }

    @Override
    protected void writeInternal(WmsFeatureInfo info, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("FeatureInfoResponse");
            
            info.getLayers().stream().flatMap((l)-> l.getFeatures().stream()).forEach((f) -> {
                Element fields = doc.createElement("FIELDS");
                f.getAttributes().forEach(fields::setAttribute);
                rootElement.appendChild(fields);
            });
            
            doc.appendChild(rootElement);
            transformer.transform(new DOMSource(doc), new StreamResult(outputMessage.getBody()));
        } catch (TransformerException | ParserConfigurationException ex) {
            throw new IOException("Failed to write xml response", ex);
        }
    }
}
