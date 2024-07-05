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
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.network.NetworkIndexingService;
import uk.ac.ceh.gateway.catalogue.model.CodeDocument;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.indexing.ClassMap;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.PrioritisedClassMap;
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
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
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
import java.util.List;

import static java.util.stream.Stream.of;
import static org.springframework.http.MediaType.TEXT_HTML;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.GEMINI_XML;

@Slf4j
@Configuration
public class IndexingServicesConfig {

    @Bean @Qualifier("network-index")
    public DocumentIndexingService networkIndexingService(
            BundledReaderService<MetadataDocument> bundledReaderService
    ){
        return new NetworkIndexingService(bundledReaderService);
    }
    @Bean @Qualifier("datacite-index")
    public DocumentIndexingService dataciteIndexingService(
            BundledReaderService<MetadataDocument> bundledReaderService,
            DataciteService dataciteService
            ) {
        return new DataciteIndexingService(bundledReaderService, dataciteService);
            }

    @Bean @Qualifier("jena-index")
    public JenaIndexingService jenaIndexingService(
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
            .register(ElterDocument.class, new JenaIndexElterDocumentGenerator(documentGenerator, baseUri))
            .register(CodeDocument.class, new JenaIndexCodeDocumentGenerator(documentGenerator, baseUri))
            .register(InfrastructureRecord.class, new JenaIndexInfrastructureRecordGenerator(documentGenerator, baseUri))
            .register(LinkDocument.class, new JenaIndexLinkDocumentGenerator(documentGenerator))
            .register(MonitoringFacility.class, new JenaIndexMonitoringFacilityGenerator(documentGenerator, baseUri))
            .register(MetadataDocument.class, documentGenerator);

        return new JenaIndexingService(
                bundledReaderService,
                documentListingService,
                dataRepository,
                new IndexGeneratorRegistry<>(mappings),
                documentIdentifierService,
                tdbModel
                );
            }

    @Bean @Qualifier("mapserver-index")
    public MapServerIndexingService mapServerIndexingService(
            BundledReaderService<MetadataDocument> bundledReaderService,
            DataRepository<CatalogueUser> dataRepository,
            DocumentListingService documentListingService,
            freemarker.template.Configuration freemarkerConfiguration,
            @Value("${maps.location}") File mapsLocation,
            MapServerDetailsService mapServerDetailsService
            ) {
        val generator = new MapServerIndexGenerator(freemarkerConfiguration, mapServerDetailsService);
        return new MapServerIndexingService(
                bundledReaderService,
                documentListingService,
                dataRepository,
                generator,
                mapsLocation);
            }

    @Bean @Qualifier("solr-index")
    public SolrIndexingService solrIndexingService(
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
            .register(SampleArchive.class, new SampleArchiveIndexGenerator(metadataDocumentGenerator))
            .register(InfrastructureRecord.class, new InfrastructureRecordIndexGenerator(metadataDocumentGenerator))
            .register(LinkDocument.class, linkDocumentGenerator)
            .register(MetadataDocument.class, metadataDocumentGenerator);

        IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry = new IndexGeneratorRegistry<>(mappings);
        linkDocumentGenerator.setIndexGeneratorRegistry(indexGeneratorRegistry);
        log.info("Set repository & registry on {}", linkDocumentGenerator);

        return new SolrIndexingService(
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
    @SneakyThrows
    public ValidationIndexingService validationIndexingService(
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

        val htmlValidator = new MediaTypeValidator("HTML Generation", TEXT_HTML, documentWritingService);
        val schemaValidator = new XSDSchemaValidator("Gemini", GEMINI_XML, documentWritingService, geminiSchema);

        val mappings = new PrioritisedClassMap<IndexGenerator<?, ValidationReport>>()
            .register(GeminiDocument.class, new ValidationIndexGenerator(List.of(
                            schemaValidator,
                            htmlValidator
                            )))
            .register(MetadataDocument.class, new ValidationIndexGenerator(List.of(htmlValidator)));

        return new ValidationIndexingService(
                bundledReaderService,
                documentListingService,
                dataRepository,
                postProcessingService,
                documentIdentifierService,
                new IndexGeneratorRegistry<>(mappings)
                );
            }
}
