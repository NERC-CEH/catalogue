package uk.ac.ceh.gateway.catalogue.elter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.springframework.http.MediaType;

import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class DummyLinkedElterDocument extends AbstractMetadataDocument {
    private String linkedDocumentUri;
    private String linkedDocumentType;
}
