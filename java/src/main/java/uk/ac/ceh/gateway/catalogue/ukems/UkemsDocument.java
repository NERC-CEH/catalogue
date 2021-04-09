package uk.ac.ceh.gateway.catalogue.ukems;

import java.util.List;

import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/ukems/ukems-document.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/erddap.tplx", whenRequestedAs=ERDDAP_XML_VALUE)
})
public class UkemsDocument extends GeminiDocument {}
