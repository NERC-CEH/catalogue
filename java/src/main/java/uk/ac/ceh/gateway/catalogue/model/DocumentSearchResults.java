package uk.ac.ceh.gateway.catalogue.model;

import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocumentSolrIndexGenerator;

/**
 * A search results object for documents
 * @author jcoop, cjohn
 */
@ConvertUsing({
    @Template(called = "/html/search.html.tpl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
public class DocumentSearchResults extends SearchResults<GeminiDocumentSolrIndexGenerator.DocumentSolrIndex> {
    
}
