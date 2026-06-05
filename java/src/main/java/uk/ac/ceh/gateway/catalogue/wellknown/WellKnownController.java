package uk.ac.ceh.gateway.catalogue.wellknown;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@ToString
@RestController
@Tag(name = "Discovery", description = "Machine-readable service description endpoints")
public class WellKnownController {

    private final String voidDocument;

    public WellKnownController(@Value("${documents.baseUri}") String baseUri) {
        this.voidDocument = """
            @prefix void:    <http://rdfs.org/ns/void#> .
            @prefix foaf:    <http://xmlns.com/foaf/0.1/> .
            @prefix dcterms: <http://purl.org/dc/terms/> .

            <%s>
                a void:Dataset ;
                dcterms:title "EIDC Metadata Catalogue"@en ;
                foaf:homepage <%s> ;
                void:sparqlEndpoint <%s/sparql> ;
                void:dataDump <%s/catalogue.ttl> ;
                .
            """.formatted(baseUri, baseUri, baseUri, baseUri);
        log.info("Creating");
    }

    @Operation(
        summary = "VoID dataset description",
        description = "W3C VoID description of the catalogue dataset, declaring the SPARQL endpoint and data dumps for automated discovery"
    )
    @GetMapping(value = ".well-known/void", produces = "text/turtle")
    public ResponseEntity<String> getVoidDescription() {
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("text/turtle"))
            .body(voidDocument);
    }
}
