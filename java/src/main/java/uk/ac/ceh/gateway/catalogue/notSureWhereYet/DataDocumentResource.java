package uk.ac.ceh.gateway.catalogue.notSureWhereYet;

import java.io.IOException;
import java.io.InputStream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.io.AbstractResource;
import uk.ac.ceh.components.datastore.DataDocument;

/**
 *
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class DataDocumentResource extends AbstractResource {
    private final DataDocument document;

    @Override
    public String getDescription() {
        return document.getRevision() + document.getFilename();
    }

    @Override
    public boolean exists() {
            return true;
    }

    @Override
    public long contentLength() throws IOException {
        return document.length();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return document.getInputStream();
    }
}