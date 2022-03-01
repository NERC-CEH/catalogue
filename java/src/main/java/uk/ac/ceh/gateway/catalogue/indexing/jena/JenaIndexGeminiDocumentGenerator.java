package uk.ac.ceh.gateway.catalogue.indexing.jena;

import com.google.common.base.Strings;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;

import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * The following class extracts semantic details from a GeminiDocument and
 * returns these as Jena Statements (triples)
 */
@Slf4j
@ToString
public class JenaIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;
    private final String baseUri;
    
    private static final Pattern downloadPattern = Pattern
        .compile(
            "^(https:\\/\\/data-package\\.ceh\\.ac\\.uk\\/data\\/.+)|(https:\\/\\/catalogue\\.ceh\\.ac\\.uk\\/datastore\\/eidchub\\/.+)|(https:\\/\\/order-eidc\\.ceh\\.ac\\.uk\\/resources\\/\\w+\\/order)$",
            CASE_INSENSITIVE
        );

    public JenaIndexGeminiDocumentGenerator(JenaIndexMetadataDocumentGenerator generator, String baseUri) {
        this.generator = generator;
        this.baseUri = baseUri;
        log.info("Creating {}", this);
    }

    @Override
    public List<Statement> generateIndex(GeminiDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);

        Resource me = generator.resource(document.getId());

        String recordType = document.getType();
        
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself
        
        //Only if recordType = dataset/nongeographic dataset
        toReturn.add(createStatement(me, RDF_TYPE, DCAT_DATASET_CLASS));


        Optional.ofNullable(document.getBoundingBoxes())
            .orElse(Collections.emptyList())
            .forEach(b ->
                toReturn.add(createStatement(me, HAS_GEOMETRY, createTypedLiteral(b.getWkt(), WKT_LITERAL)))
            );

        Optional.ofNullable(document.getResourceIdentifiers())
            .orElse(Collections.emptyList())
            .stream()
            .filter(r -> !r.getCoupledResource().isEmpty())
            .forEach(r ->
                toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(r.getCoupledResource())))
            );


        Optional.ofNullable(document.getCoupledResources())
            .orElse(Collections.emptyList())
            .stream()
            .filter(r -> !r.isEmpty())
            .forEach(r ->
                toReturn.add(createStatement(me, EIDCUSES, createResource(r)))
            );
        

        Optional.ofNullable(document.getRelatedRecords())
            .orElse(Collections.emptyList())
            .stream()
            .filter(rr -> !Strings.isNullOrEmpty(rr.getRel()))
            .filter(rr -> !Strings.isNullOrEmpty(rr.getIdentifier()))
            .forEach(rr -> {
                log.debug(rr.toString());
                toReturn.add(createStatement(
                    me,
                    createProperty(rr.getRel()),
                    createProperty(baseUri + "/id/" + rr.getIdentifier())
                ));
            });

        Optional.ofNullable(document.getOnlineResources())
            .orElse(Collections.emptyList())
            .stream()
            .filter(onliner -> !Strings.isNullOrEmpty(onliner.getUrl()))
            .filter(onliner -> downloadPattern.matcher(onliner.getUrl()).matches())
            .forEach(onliner -> {
                log.debug(onliner.toString());
                Resource dist = generator.resource(document.getId() + "_dist");
                //:dist needs to be unique eg :<id>_dist_<sequentialNumber>
                toReturn.add(createStatement(dist, RDF_TYPE, DCAT_DISTRIBUTION_CLASS));
                toReturn.add(createStatement(dist, DCAT_ACCESSURL, createProperty(onliner.getUrl())));
                toReturn.add(createStatement(me, DCAT_DISTRIBUTION, dist));
            });

        //Also want to add publication date:
        //toReturn.add(createStatement(me, DCT_ISSUED, --publication date--, TYPE_DATE))); 



        return toReturn;
    }
}
