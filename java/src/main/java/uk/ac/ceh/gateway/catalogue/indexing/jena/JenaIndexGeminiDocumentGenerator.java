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

    //added these for testing - they should be local rather than global counters
    private int authorCount = 0;
    private int onlineResourceCount = 0;

    private static final Pattern downloadPattern = Pattern
        .compile(
            "^(https:\\/\\/data-package\\.ceh\\.ac\\.uk\\/data\\/.+)|(https:\\/\\/catalogue\\.ceh\\.ac\\.uk\\/datastore\\/eidchub\\/.+)|(https:\\/\\/order-eidc\\.ceh\\.ac\\.uk\\/resources\\/\\w+\\/order)$",
            CASE_INSENSITIVE
        );

    private static final Pattern orcidPattern = Pattern
        .compile("^http[s]?:\\/\\/orcid\\.org\\/0000(-\\d{4}){2}-\\d{3}[\\dX]$")
        ;
    
    private static final Pattern doiPattern = Pattern
        .compile("^http[s]?:\\/\\/(dx\\.)?doi\\.org\\/10\\.\\d+\\/\\S+")
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
        
        Optional.ofNullable(document.getDatasetReferenceDate().getPublicationDate())
            .ifPresent(pd -> toReturn.add(
                createStatement(me, DCT_ISSUED, createTypedLiteral(pd.toString(), TYPE_DATE)))
            );

        Optional.ofNullable(document.getResourceStatus())
            .ifPresent(status -> toReturn.add(
                createStatement(me, ADMD_STATUS, createPlainLiteral(status)))
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

        //Authors with orcids
        Optional.ofNullable(document.getAuthors())
            .orElse(Collections.emptyList())
            .stream()
            .filter(author -> author.getRole().equalsIgnoreCase("author"))
            .filter(author -> orcidPattern.matcher(author.getNameIdentifier()).matches())
            .forEach(author -> {
                log.debug(author.toString());
                Resource author_node = createProperty(author.getNameIdentifier());
                toReturn.add(createStatement(me, DCT_CREATOR, author_node));
                toReturn.add(createStatement(author_node, RDF_TYPE, VCARD_CLASS_INDIVIDUAL));
                toReturn.add(createStatement(author_node, VCARD_NAME, createPlainLiteral(author.getIndividualName())));
                toReturn.add(createStatement(author_node, VCARD_ORGNAME, createPlainLiteral(author.getOrganisationName())));
            });

        //Authors without orcids
        Optional.ofNullable(document.getAuthors())
            .orElse(Collections.emptyList())
            .stream()
            .filter(author -> author.getRole().equalsIgnoreCase("author"))
            .filter(author -> !orcidPattern.matcher(author.getNameIdentifier()).matches())
            .forEach(author -> {
                log.debug(author.toString());
                Resource author_node = generator.resource(document.getId() + "_author_" + authorCount);
                toReturn.add(createStatement(me, DCT_CREATOR, author_node));
                toReturn.add(createStatement(author_node, RDF_TYPE, VCARD_CLASS_INDIVIDUAL));
                toReturn.add(createStatement(author_node, VCARD_NAME, createPlainLiteral(author.getIndividualName())));
                toReturn.add(createStatement(author_node, VCARD_ORGNAME, createPlainLiteral(author.getOrganisationName())));
                authorCount++;
            });

        //Publisher
        Optional.ofNullable(document.getResponsibleParties())
            .orElse(Collections.emptyList())
            .stream()
            .filter(rp -> rp.getRole().equalsIgnoreCase("publisher"))
            .filter(rp -> rorPattern.matcher(rp.getNameIdentifier()).matches())
            .forEach(rp -> {
                log.debug(rp.toString());
                Resource publisher_node = createProperty(rp.getOrganisationIdentifier());
                toReturn.add(createStatement(me, DCAT_PUBLISHER, publisher_node));
                toReturn.add(createStatement(publisher_node, RDF_TYPE, FOAF_CLASS_ORGANISATION));
                toReturn.add(createStatement(publisher_node, VCARD_ORGNAME, createPlainLiteral(rp.getOrganisationName())));
            });

        Optional.ofNullable(document.getOnlineResources())
            .orElse(Collections.emptyList())
            .stream()
            .filter(onliner -> !Strings.isNullOrEmpty(onliner.getUrl()))
            .filter(onliner -> downloadPattern.matcher(onliner.getUrl()).matches())
            .forEach(onliner -> {
                log.debug(onliner.toString());
                Resource dist_node = generator.resource(document.getId() + "_dist_" + onlineResourceCount);
                toReturn.add(createStatement(me, DCAT_DISTRIBUTION, dist_node));
                toReturn.add(createStatement(dist_node, RDF_TYPE, DCAT_CLASS_DISTRIBUTION));
                toReturn.add(createStatement(dist_node, DCAT_ACCESSURL, createProperty(onliner.getUrl())));
                //add licence info dist_node <http://purl.org/dc/terms/licence> useConstraints.uri
                //add accessrights dist_node <http://purl.org/dc/terms/accessRights> accessLimitation.uri
                //add format dist_node <http://purl.org/dc/terms/format> distributionFormat.type - multiple formats are possible
                onlineResourceCount++;
            });

        Optional.ofNullable(document.getSupplemental())
            .orElse(Collections.emptyList())
            .stream()
            .filter(citation -> !Strings.isNullOrEmpty(citation.getUrl()))
            .filter(citation -> citation.getFunction().equalsIgnoreCase("isReferencedBy"))
            .filter(citation -> doiPattern.matcher(citation.getUrl()).matches())
            .forEach(citation -> {
                log.debug(citation.toString());
                toReturn.add(createStatement(me, CITO_CITEDBY, createProperty(citation.getUrl())));
            });

        // NEED to add keywords as http://www.w3.org/ns/dcat#theme
        // me <http://www.w3.org/ns/dcat#theme> <uri>; rdfs:label "Literal"

        return toReturn;
    }
}
