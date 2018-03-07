package uk.ac.ceh.gateway.catalogue.elter;

import org.springframework.http.MediaType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/elter/person.html.tpl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
public class PersonDocument extends AbstractMetadataDocument {

}