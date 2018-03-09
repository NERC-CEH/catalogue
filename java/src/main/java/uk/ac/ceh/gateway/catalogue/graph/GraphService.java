package uk.ac.ceh.gateway.catalogue.graph;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.xerces.util.URI;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@AllArgsConstructor
public class GraphService {

    private static String sparql = "PREFIX dc: <http://purl.org/dc/terms/> " +
    "SELECT ?id ?title ?rel ?dir " +
    "WHERE {{ " +
        "BIND(\"child\" AS ?dir) . " +
        "?me ?rel ?node . " +
        "?node dc:title ?title . " +
        "?node dc:identifier ?id . " +
    "} UNION { " +
        "BIND(\"parent\" AS ?dir) . " +
        "?node ?rel ?me . " +
        "?node dc:title ?title . " +
        "?node dc:identifier ?id . " +
    "}}";

    private final Dataset jenaTdb;

    public Graph shallow(String id, String title, String uri) {
        val builder = new GraphBuilder();
        builder
            .node(id)
                .label(title)
                .classes("root")
            .add();
        
        ParameterizedSparqlString pss = new ParameterizedSparqlString(sparql);
        pss.setIri("me", uri);
        jenaTdb.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec
              .execSelect()
              .forEachRemaining(solution -> {
                
                val foundTitle = solution.get("title").toString();
                val foundId = solution.get("id").toString();
                val foundRelationship = solution.get("rel").toString();
                val direction =  solution.get("dir").toString();

                String source = foundId;
                String target = id;
                if (direction.equals("child")) {
                    source = id;
                    target = foundId;
                }

                builder
                    .node(foundId)
                        .label(foundTitle)
                        .classes(direction)
                    .add()
                    .node(String.format("%s-%s", source, target))
                        .label(uriValue(foundRelationship))
                        .source(source)
                        .target(target)
                        .classes(uriValue(foundRelationship))
                    .add();
              });
        } finally {
            jenaTdb.end();
        }

        return builder.build();
    }

    @SneakyThrows
    private String uriValue(String value) {
        URI uri = new URI(value);
        String path = uri.getPath();
        return  path.substring(path.lastIndexOf('/') + 1);
    }
}