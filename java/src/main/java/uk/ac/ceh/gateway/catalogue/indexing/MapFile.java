package uk.ac.ceh.gateway.catalogue.indexing;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.Writer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 * The following defines a map file representation of a metadata document.
 * It provides the method for writing a map file to a given output writer.
 * @author cjohn
 */
@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class MapFile {
    private final Configuration templateConfiguration;
    private final String template;
    @Getter private final MetadataDocument document;
    
    /**
     * Write the given metadata documents mapfile to the supplied writer.
     * @param out to write the mapfile to
     * @throws TemplateException if there is an issue with the template
     * @throws IOException if there is a problem writing the map file
     */
    public void writeTo(Writer out) throws TemplateException, IOException {
        templateConfiguration.getTemplate(template).process(document, out);
    }
}
