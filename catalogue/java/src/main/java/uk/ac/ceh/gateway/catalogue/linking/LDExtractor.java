package uk.ac.ceh.gateway.catalogue.linking;

import com.hp.hpl.jena.rdf.model.Statement;
import java.util.List;

/**
 *
 * @author cjohn
 * @param <D> The type of document this extractor can read
 */
public interface LDExtractor<D> {
    List<Statement> getStatements(D document);
}
