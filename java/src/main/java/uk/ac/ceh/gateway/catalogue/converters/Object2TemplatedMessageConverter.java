package uk.ac.ceh.gateway.catalogue.converters;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class Object2TemplatedMessageConverter<T> extends AbstractHttpMessageConverter<T> {
    private final Configuration configuration;
    @ToString.Include
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

    @SneakyThrows
    @Override
    public List<MediaType> getSupportedMediaTypes() {
        val templates = Optional.ofNullable(clazz.getAnnotation(ConvertUsing.class))
            .orElseThrow(() -> new HttpMediaTypeNotSupportedException("No media types"))
            .value();

        return Arrays.stream(templates)
            .map(template -> MediaType.parseMediaType(template.whenRequestedAs()))
            .collect(Collectors.toList());
    }

    @Override
    protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            //Locate the correct template for the given media type
            val templateName = Arrays.stream(getTemplates(t))
                .filter(template -> isCompatibleMediaType(template, outputMessage))
                .findFirst()
                .orElseThrow(() -> new HttpMessageNotWritableException("No suitable template"))
                .called();
            val freemarkerTemplate = configuration.getTemplate(templateName);
            val processedTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, t);
            log.debug("Using: {} for: {}", templateName, outputMessage.getHeaders().getContentType());
            log.debug(processedTemplate);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(outputMessage.getBody(), StandardCharsets.UTF_8))) {
                writer.write(processedTemplate);
            }
        } catch(TemplateException | ParseException e) {
            log.error(t.toString());
            throw new HttpMessageNotWritableException("There was an error in the template", e);
        }
    }

    private Template[] getTemplates(Object t) {
        return t.getClass().getAnnotation(ConvertUsing.class).value();
    }

    private boolean isCompatibleMediaType(Template template, HttpOutputMessage outputMessage) {
        val requestedMediaType = outputMessage.getHeaders().getContentType();
        val templateMediaType = MediaType.parseMediaType(template.whenRequestedAs());
        return templateMediaType.isCompatibleWith(requestedMediaType);
    }
}
