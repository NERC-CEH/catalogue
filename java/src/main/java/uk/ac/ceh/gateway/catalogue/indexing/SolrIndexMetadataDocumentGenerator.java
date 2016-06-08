package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.base.Strings;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

/**
 * The following class is responsible for taking a gemini document and creating 
 * beans which are solr indexable
 * @author cjohn
 */
public class SolrIndexMetadataDocumentGenerator implements IndexGenerator<MetadataDocument, SolrIndex> {
    private final CodeLookupService codeLookupService;
    private final DocumentIdentifierService identifierService;
    private final Dataset jenaTdb;
    
    public SolrIndexMetadataDocumentGenerator(CodeLookupService codeLookupService, DocumentIdentifierService identifierService, Dataset jenaTdb) {
        this.codeLookupService = codeLookupService;
        this.identifierService = identifierService;
        this.jenaTdb = jenaTdb;
    }

    @Override
    public SolrIndex generateIndex(MetadataDocument document) {
        return new SolrIndex()
                .setDescription(document.getDescription())
                .setTitle(document.getTitle())
                .setIdentifier(identifierService.generateFileId(document.getId()))
                .setResourceType(codeLookupService.lookup("metadata.resourceType", document.getType()))
                .setState(getState(document))
                .setView(getViews(document))
                .setRepository(getRepository(document));
    }
    
    private String getState(MetadataDocument document) {
        if (document.getMetadata() != null) {
            return document.getMetadata().getState();
        } else {
            return null;
        }
    }
    
    private List<String> getViews(MetadataDocument document) {
        Objects.requireNonNull(document);
        return Optional.ofNullable(document)
            .map(MetadataDocument::getMetadata)
            .map(m -> m.getIdentities(Permission.VIEW))
            .orElse(Collections.emptyList());       
    }
    
    // The following will iterate over a given collection (which could be null)
    // And grab a property off of each element in the collection.
    // If the supplied collection is null, this method will return an empty
    // list
    public static <T> List<String> grab(Collection<T> list, Function<? super T, String> mapper ) {
        return Optional.ofNullable(list)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(mapper)
                        .map(Strings::emptyToNull)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
    }
    
    private List<String> getRepository(MetadataDocument document) {
        List<String> toReturn = new ArrayList<>();
        Optional.ofNullable(document.getUri()).ifPresent(uri -> {
            ParameterizedSparqlString pss = new ParameterizedSparqlString(
                "SELECT ?title " +
                "WHERE { " +
                "?me <http://purl.org/dc/terms/isPartOf> _:a . " +
                "_:a <http://purl.org/dc/terms/title> ?title ; " +
                "    <http://purl.org/dc/terms/type> 'repository' " +
                "}"
            );
            pss.setIri("me", uri.toString());
            
            if (jenaTdb.isInTransaction()) {
                try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
                    qexec.execSelect().forEachRemaining(s -> {
                        toReturn.add(s.getLiteral("title").getString());
                    });
                }
            } else {
                jenaTdb.begin(ReadWrite.READ); 
                try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
                    qexec.execSelect().forEachRemaining(s -> {
                        toReturn.add(s.getLiteral("title").getString());
                    });
                } finally {
                    jenaTdb.end();
                } 
            }
        
        });
        
        return toReturn;
            
    } 
             
}
