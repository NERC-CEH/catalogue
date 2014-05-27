package uk.ac.ceh.gateway.catalogue.converters;

import freemarker.template.Configuration;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 *
 * @author cjohn
 */
public class Object2TemplatedMessageConverter implements HttpMessageConverter<Object> {
    
    private final Configuration configuration;
    
    public Object2TemplatedMessageConverter(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false; // I can't read anything
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        ConvertUsing using = clazz.getAnnotation(ConvertUsing.class);
        if(using != null) {
            return Arrays.stream(using.value())                    
                  .anyMatch(t-> MediaType.parseMediaType(t.whenRequestedAs()).isCompatibleWith(mediaType));
        }
        else {
            return false;
        }
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return null; //not sure what to return here
    }

    @Override
    public Object read(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(Object t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
