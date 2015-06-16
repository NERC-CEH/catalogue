package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import javax.xml.bind.*;
import javax.xml.transform.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.util.ClassUtils;

/**
 * The following abstracts Springs Jaxb2RootElementHttpMessageConverter and allows
 * documents to be read in the format in which they were sent. That is the full 
 * subclass.
 * 
 * This class therefore adheres the @XmlAlsoSee annotation
 * @author Christopher Johnson
 */
public class Jaxb2HttpMessageConverter extends Jaxb2RootElementHttpMessageConverter {
    private final String namespace, schemaLocation;
    
    public Jaxb2HttpMessageConverter(String namespace, String schemaLocation) {
        this.namespace = checkNotNull(emptyToNull(namespace));
        this.schemaLocation = checkNotNull(emptyToNull(schemaLocation));
    }
    
    @Override
    protected Object readFromSource(Class<?> clazz, HttpHeaders headers, Source source) throws IOException {
        try {
            Unmarshaller unmarshaller = createUnmarshaller(clazz);
            return unmarshaller.unmarshal(source);
        } catch (UnmarshalException ex) {
            throw new HttpMessageNotReadableException("Could not unmarshal to [" + clazz + "]: " + ex.getMessage(), ex);
        } catch (JAXBException ex) {
            throw new HttpMessageConversionException("Could not instantiate JAXBContext: " + ex.getMessage(), ex);
        }
    }
    
    @Override
    protected void writeToResult(Object o, HttpHeaders headers, Result result) throws IOException {
        try {
            Class clazz = ClassUtils.getUserClass(o);
            Marshaller marshaller = createMarshaller(clazz);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, String.format("%s %s", namespace, schemaLocation));
            setCharset(headers.getContentType(), marshaller);
            marshaller.marshal(o, result);
        } catch (MarshalException ex) {
            throw new HttpMessageNotWritableException("Could not marshal [" + o + "]: " + ex.getMessage(), ex);
        } catch (JAXBException ex) {
            throw new HttpMessageConversionException("Could not instantiate JAXBContext: " + ex.getMessage(), ex);
        }
    }

    private void setCharset(MediaType contentType, Marshaller marshaller) throws PropertyException {
        if (contentType != null && contentType.getCharSet() != null) {
            marshaller.setProperty(Marshaller.JAXB_ENCODING, contentType.getCharSet().name());
        }
    }
}