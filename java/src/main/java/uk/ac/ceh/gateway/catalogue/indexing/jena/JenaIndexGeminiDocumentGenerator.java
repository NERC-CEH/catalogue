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

    private static final Pattern orcidPattern = Pattern
        .compile("^http[s]?:\\/\\/orcid\\.org\\/0000(-\\d{4}){2}-\\d{3}[\\dX]$")
        ;
    
    private static final Pattern rorPattern = Pattern
        .compile("^http[s]?:\\/\\/ror\\.org\\/\\w{9}$")
        ;


    public JenaIndexGeminiDocumentGenerator(JenaIndexMetadataDocumentGenerator generator, String baseUri) {
        this.generator = generator;
        this.baseUri = baseUri;
        log.info("Creating {}", this);
    }

    @Override
    public List<Statement> generateIndex(GeminiDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);

        Resource me = generator.resource(document.getId());
        
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself
        
        // This isn't working.  I don't know why.
        Optional.ofNullable(document.getType())
            .ifPresent(type -> {
                if (type.trim().equals("dataset") || type.trim().equals("nonGeographicDataset") ) {
                    createStatement(me, RDF_TYPE, DCAT_DATASET_CLASS);
                }
            });

        Optional.ofNullable(document.getDatasetReferenceDate().getPublicationDate())
            .ifPresent(pd -> toReturn.add(
                createStatement(me, DCT_ISSUED, createTypedLiteral(pd.toString(), TYPE_DATE)))
            );

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

        //authors with orcids
        Optional.ofNullable(document.getResponsibleParties())
            .orElse(Collections.emptyList())
            .stream()
            .filter(author -> !Strings.isNullOrEmpty(author.getIndividualName()))
            //.filter(author -> author.getRole().equalsIgnoreCase("author"))
            //.filter(author -> orcidPattern.matcher(author.getNameIdentifier()).matches())
            .forEach(author -> {
                log.debug(author.toString());
                toReturn.add(createStatement(createProperty(author.getNameIdentifier()), RDF_TYPE , VCARD_INDIVIDUAL_CLASS));
                toReturn.add(createStatement(createProperty(author.getNameIdentifier()), VCARD_NAME, createPlainLiteral(author.getIndividualName())));
                toReturn.add(createStatement(createProperty(author.getNameIdentifier()), VCARD_ORGNAME, createPlainLiteral(author.getOrganisationName())));
            });

        Optional.ofNullable(document.getOnlineResources())
            .orElse(Collections.emptyList())
            .stream()
            .filter(onliner -> !Strings.isNullOrEmpty(onliner.getUrl()))
            .filter(onliner -> downloadPattern.matcher(onliner.getUrl()).matches())
            .forEach(onliner -> {
                log.debug(onliner.toString());
                Resource dist_uri = generator.resource(document.getId() + "_dist");
                //dist_uri needs to be unique eg : document.getId() + "_dist" + counter
                toReturn.add(createStatement(dist_uri, RDF_TYPE, DCAT_DISTRIBUTION_CLASS));
                toReturn.add(createStatement(dist_uri, DCAT_ACCESSURL, createProperty(onliner.getUrl())));
                toReturn.add(createStatement(me, DCAT_DISTRIBUTION, dist_uri));
            });
 

        return toReturn;
    }
}
