package uk.ac.ceh.gateway.catalogue.upload;

import java.util.Map;

import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/upload/upload-document.html.tpl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
public class UploadDocument extends AbstractMetadataDocument {
    private final String parentId;
    private final Map<String, UploadFiles> uploadFiles;
}