package uk.ac.ceh.gateway.catalogue.converters;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;

/**
 *
 * @author cjohn
 */
public class Metadata2HtmlMessageConverter extends AbstractHttpMessageConverter<Metadata> {
    public static final MediaType HTML_MEDIATYPE = MediaType.TEXT_HTML;
    
    private final Configuration configuration;
    
    public Metadata2HtmlMessageConverter(Configuration configuration) {
        super(HTML_MEDIATYPE);
        this.configuration = configuration;
    }
    
    @Override
    protected boolean supports(Class<?> type) {
        return Metadata.class.isAssignableFrom(type);
    }

    @Override
    protected void writeInternal(Metadata t, HttpOutputMessage hom) throws IOException, HttpMessageNotWritableException {
        try ( Writer writer = new OutputStreamWriter(hom.getBody()) ) {
            try {
                configuration.getTemplate("metadata.tpl").process(t, writer);
            } catch (TemplateException ex) {
                throw new HttpMessageNotWritableException("Unable to process metadata into html", ex);
            }
        }
    }

    @Override
    protected Metadata readInternal(Class<? extends Metadata> type, HttpInputMessage him) throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("This converter can only write HTML of metadata documents not read them");
    }
}
