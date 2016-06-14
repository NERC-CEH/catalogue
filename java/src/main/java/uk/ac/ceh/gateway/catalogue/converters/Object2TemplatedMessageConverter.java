package uk.ac.ceh.gateway.catalogue.converters;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

/**
 *
 * @author cjohn
 */
public class Object2TemplatedMessageConverter<T> extends AbstractHttpMessageConverter<T> {
    
    private final Configuration configuration;
    private final Class<T> clazz;
    
    public Object2TemplatedMessageConverter(Class<T> clazz, Configuration configuration) {
        this.clazz = clazz;
        this.configuration = configuration;
    }  

    @Override
    protected boolean supports(Class<?> supportedClazz) {
        return clazz.isAssignableFrom(supportedClazz);
    }
    
    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("This implementation can not read anything");
    }
    
    @Override
    public List<MediaType> getSupportedMediaTypes() {
        ConvertUsing convertUsing = clazz.getAnnotation(ConvertUsing.class);
        List<MediaType> toReturn = new ArrayList<>();
        if(convertUsing != null) {
            for(Template template : convertUsing.value()) {
                toReturn.add(MediaType.parseMediaType(template.whenRequestedAs()));
            }
        }
        return toReturn;
    }
    
    @Override
    protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            //Locate the correct template for the given media type
            MediaType requestedMediaType = outputMessage.getHeaders().getContentType();
            for(Template template: t.getClass().getAnnotation(ConvertUsing.class).value()) {
                if(MediaType.parseMediaType(template.whenRequestedAs()).isCompatibleWith(requestedMediaType)) {
                    freemarker.template.Template freemarkerTemplate = configuration.getTemplate(template.called());
                    String processedTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, t);
                    try (Writer writer = new BufferedWriter(new OutputStreamWriter(outputMessage.getBody(), "UTF-8"))) {
                        writer.write(processedTemplate);
                    }
                }
            }
        } catch(TemplateException te) {
            throw new HttpMessageNotWritableException("There was an error in the template", te);
        }
    }
}