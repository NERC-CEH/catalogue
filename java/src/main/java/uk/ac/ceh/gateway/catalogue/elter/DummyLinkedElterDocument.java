package uk.ac.ceh.gateway.catalogue.elter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.springframework.http.MediaType;

import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
        @Template(called = "html/gemini.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
public class DummyLinkedElterDocument extends AbstractMetadataDocument {
    private String linkedDocumentUri;
    private String linkedDocumentType;
}
