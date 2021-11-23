package uk.ac.ceh.gateway.catalogue.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Statement;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.ClassMap;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.indexing.async.AsyncDocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.datacite.DataciteIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.jena.*;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.*;
import uk.ac.ceh.gateway.catalogue.indexing.validation.ValidationIndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.validation.ValidationIndexingService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.validation.MediaTypeValidator;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.XSDSchemaValidator;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Stream.of;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.GEMINI_XML_VALUE;

@Slf4j
@Configuration
public class IndexingServicesConfig {

    @Bean @Qualifier("datacite-index")
    public DocumentIndexingService dataciteIndexingService(
        BundledReaderService<MetadataDocument> bundledReaderService,
        DataciteService dataciteService
    ) {
        return new AsyncDocumentIndexingService(
            new DataciteIndexingService(bundledReaderService, dataciteService)
        );
    }

    @Bean(initMethod = "initialIndex") @Qualifier("jena-index")
    public JenaIndexingService<MetadataDocument> jenaIndexingService(
        @Value("${documents.baseUri}") String baseUri,
        BundledReaderService<MetadataDocument> bundledReaderService,
        DataRepository<CatalogueUser> dataRepository,
        DocumentIdentifierService documentIdentifierService,
        DocumentListingService documentListingService,
        Dataset tdbModel
    ) {
        JenaIndexMetadataDocumentGenerator documentGenerator = new JenaIndexMetadataDocumentGenerator(documentIdentifierService);

        ClassMap<IndexGenerator<?, List<Statement>>> mappings = new PrioritisedClassMap<IndexGenerator<?, List<Statement>>>()
            .register(BaseMonitoringType.class, new JenaIndexBaseMonitoringTypeGenerator(documentGenerator))
            .register(GeminiDocument.class, new JenaIndexGeminiDocumentGenerator(documentGenerator, baseUri))
            .register(LinkDocument.class, new JenaIndexLinkDocumentGenerator(documentGenerator))
            .register(MetadataDocument.class, documentGenerator);

        return new JenaIndexingService<>(
            bundledReaderService,
            documentListingService,
            dataRepository,
            new IndexGeneratorRegistry<>(mappings),
            documentIdentifierService,
            tdbModel
        );
    }

    @Bean(initMethod = "initialIndex") @Qualifier("mapserver-index")
    public MapServerIndexingService<MetadataDocument> mapServerIndexingService(
        BundledReaderService<MetadataDocument> bundledReaderService,
        DataRepository<CatalogueUser> dataRepository,
        DocumentListingService documentListingService,
        freemarker.template.Configuration freemarkerConfiguration,
        @Value("${maps.location}") File mapsLocation,
        MapServerDetailsService mapServerDetailsService
    ) {
        val generator = new MapServerIndexGenerator(freemarkerConfiguration, mapServerDetailsService);
        return new MapServerIndexingService<>(
            bundledReaderService,
            documentListingService,
            dataRepository,
            generator,
            mapsLocation);
    }

    @Bean(initMethod = "initialIndex") @Qualifier("solr-index")
    public SolrIndexingService<MetadataDocument> solrIndexingService(
        BundledReaderService<MetadataDocument> bundledReaderService,
        CodeLookupService codeLookupService,
        DataRepository<CatalogueUser> dataRepository,
        DocumentIdentifierService documentIdentifierService,
        DocumentListingService documentListingService,
        DocumentRepository documentRepository,
        JenaLookupService jenaLookupService,
        SolrClient solrClient,
        VocabularyService vocabularyService
    ) {
        val metadataDocumentGenerator = new SolrIndexMetadataDocumentGenerator(
            codeLookupService,
            documentIdentifierService,
            vocabularyService
        );
        val linkDocumentGenerator = new SolrIndexLinkDocumentGenerator();
        linkDocumentGenerator.setRepository(documentRepository);

        val mappings = new PrioritisedClassMap<IndexGenerator<?, SolrIndex>>()
            .register(GeminiDocument.class, new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), metadataDocumentGenerator, codeLookupService))
            .register(ElterDocument.class, new SolrIndexElterDocumentGenerator(metadataDocumentGenerator))
            .register(LinkDocument.class, linkDocumentGenerator)
            .register(MetadataDocument.class, metadataDocumentGenerator);

        IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry = new IndexGeneratorRegistry<>(mappings);
        linkDocumentGenerator.setIndexGeneratorRegistry(indexGeneratorRegistry);
        log.info("Set repository & registry on {}", linkDocumentGenerator);

        return new SolrIndexingService<>(
            bundledReaderService,
            documentListingService,
            dataRepository,
            indexGeneratorRegistry,
            solrClient,
            jenaLookupService,
            documentIdentifierService
        );
    }

    @Bean
    @Qualifier("validation-index")
    public DocumentIndexingService asyncValidationIndexingService(
        ValidationIndexingService<MetadataDocument> validationIndexingService
    ) {
        return new AsyncDocumentIndexingService(validationIndexingService);
    }

    @Bean
    @SneakyThrows
    public ValidationIndexingService<MetadataDocument> validationIndexingService(
        BundledReaderService<MetadataDocument> bundledReaderService,
        DataRepository<CatalogueUser> dataRepository,
        DocumentIdentifierService documentIdentifierService,
        DocumentListingService documentListingService,
        DocumentWritingService documentWritingService,
        PostProcessingService<MetadataDocument> postProcessingService,
        @Value("${schemas.location}") String schemas
    ) {
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        val geminiSchema = schemaFactory.newSchema(
            of("gemini/srv/srv.xsd", "gemini/gmx/gmx.xsd")
                .map( (s) -> new StreamSource(new File(schemas, s)))
                .toArray(Source[]::new)
        );

        val htmlValidator = new MediaTypeValidator("HTML Generation", MediaType.TEXT_HTML, documentWritingService);

        ClassMap<IndexGenerator<?, ValidationReport>> mappings = new PrioritisedClassMap<IndexGenerator<?, ValidationReport>>()
            .register(GeminiDocument.class, new ValidationIndexGenerator(Arrays.asList(
                new XSDSchemaValidator("Gemini", MediaType.parseMediaType(GEMINI_XML_VALUE), documentWritingService, geminiSchema),
                htmlValidator
            )))
            .register(MetadataDocument.class, new ValidationIndexGenerator(Collections.singletonList(htmlValidator)));

        return new ValidationIndexingService<>(
            bundledReaderService,
            documentListingService,
            dataRepository,
            postProcessingService,
            documentIdentifierService,
            new IndexGeneratorRegistry<>(mappings)
        );
    }
}
