package uk.ac.ceh.gateway.catalogue.converters;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ToString(exclude = "configuration")
public class Object2TemplatedMessageConverter<T> extends AbstractHttpMessageConverter<T> {
    private final Configuration configuration;
    private final Class<T> clazz;

    public Object2TemplatedMessageConverter(Class<T> clazz, Configuration configuration) {
        this.clazz = clazz;
        this.configuration = configuration;
        log.info("Creating {}", this);
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
        throw new HttpMessageNotReadableException("This implementation can not read anything", inputMessage);
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
        } catch(TemplateException | ParseException e) {
            log.error(t.toString());
            throw new HttpMessageNotWritableException("There was an error in the template", e);
        }
    }
}
