package uk.ac.ceh.gateway.catalogue.postprocess;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.imp.Link;

public class ImpDocumentPostProcessingService implements PostProcessingService<ImpDocument> {
    private final Dataset jenaTdb;

    public ImpDocumentPostProcessingService(Dataset jenaTdb) {
        this.jenaTdb = jenaTdb;
    }

    @Override
    public void postProcess(ImpDocument document) throws PostProcessingException {
        Optional.ofNullable(document.getUri()).ifPresent(u -> {
            String uri = u.toString();
            if (jenaTdb.isInTransaction()) {
                process(document, uri);
            } else {
                jenaTdb.begin(ReadWrite.READ);  
                try {
                    process(document, uri);
                } finally {
                    jenaTdb.end();
                }
            }
        });
    }
    
    private void process(ImpDocument document, String uri) {
        List<Link> links = new ArrayList<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
            "SELECT ?node ?title " +
            "WHERE { " +
            "?me <http://purl.org/dc/terms/references> ?node . " +
            "?node <http://purl.org/dc/terms/title> ?title " +
            "}"
        );  
        pss.setIri("me", uri);
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec.execSelect().forEachRemaining(s -> {
                links.add(
                    new Link(
                        s.getResource("node").getURI(),
                        s.getLiteral("title").getString()
                    )
                );
            });
        }
//        document.setLinks(links);
    }
}