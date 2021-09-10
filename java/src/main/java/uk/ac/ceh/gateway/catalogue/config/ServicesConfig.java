package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.tdb.TDBFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.citation.CitationService;
import uk.ac.ceh.gateway.catalogue.converters.Gml2WmsFeatureInfoMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.UkeofXml2EFDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.converters.Xml2WmsCapabilitiesMessageConverter;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.JacksonDocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.*;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.document.writing.MessageConverterWritingService;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModel;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModelUse;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.indexing.ClassMap;
import uk.ac.ceh.gateway.catalogue.indexing.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.osdp.*;
import uk.ac.ceh.gateway.catalogue.postprocess.BaseMonitoringTypePostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.ClassMapPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapper;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyRetriever;
import uk.ac.ceh.gateway.catalogue.sparql.SparqlVocabularyService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.wms.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.wms.MapServerGetFeatureInfoErrorHandler;
import uk.ac.ceh.gateway.catalogue.ukems.UkemsDocument;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig.*;

@Slf4j
@Configuration
public class ServicesConfig {

    @Bean
    @Qualifier("normal")
    public RestTemplate normalRestTemplate(CloseableHttpClient httpClient) {
        log.info("Creating Normal RestTemplate");
        val requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    @Bean
    @Qualifier("sparql")
    public RestTemplate sparqlRestTemplate(ObjectMapper objectMapper) {
        log.info("Creating SPARQL RestTemplate");
        val messageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        val supportedMediaTypes = Arrays.asList(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "*+json"),
            new MediaType("application", "*+json-simple")
        );
        messageConverter.setSupportedMediaTypes(supportedMediaTypes);
        val restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Collections.singletonList(messageConverter));
        return restTemplate;
    }

    @Bean
    @Qualifier("wms")
    public RestTemplate getFeatureInfoRestTemplate() throws XPathExpressionException {
        log.info("Creating WMS RestTemplate");
        RestTemplate toReturn = new RestTemplate();
        toReturn.setMessageConverters(Collections.singletonList(
            new Gml2WmsFeatureInfoMessageConverter()
        ));
        toReturn.setErrorHandler(new MapServerGetFeatureInfoErrorHandler());
        return toReturn;
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public PostProcessingService postProcessingService(
        CitationService citationService,
        DataciteService dataciteService,
        org.apache.jena.query.Dataset tdbModel,
        DocumentIdentifierService documentIdentifierService
    ) {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
            .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService, dataciteService, tdbModel, documentIdentifierService))
            .register(BaseMonitoringType.class, new BaseMonitoringTypePostProcessingService(tdbModel));
        return new ClassMapPostProcessingService(mappings);
    }

    @Bean
    public DocumentReadingService documentReadingService(
        CodeLookupService codeLookupService,
        ObjectMapper objectMapper
    ) {
        return new MessageConverterReadingService()
            .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeLookupService))
            .addMessageConverter(new UkeofXml2EFDocumentMessageConverter())
            .addMessageConverter(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Bean
    public DocumentTypeLookupService metadataRepresentationService() {
        return new HashMapDocumentTypeLookupService()
            .register(GEMINI_DOCUMENT, GeminiDocument.class)
            .register(ELTER_SHORT, ElterDocument.class)
            .register(EF_DOCUMENT, BaseMonitoringType.class)
            .register(IMP_DOCUMENT, ImpDocument.class)
            .register(LINK_DOCUMENT, LinkDocument.class)
            .register(CEH_MODEL, CehModel.class)
            .register(CEH_MODEL_APPLICATION, CehModelApplication.class)
            .register(OSDP_AGENT_SHORT, Agent.class)
            .register(OSDP_DATASET_SHORT, uk.ac.ceh.gateway.catalogue.osdp.Dataset.class)
            .register(OSDP_MODEL_SHORT, uk.ac.ceh.gateway.catalogue.osdp.Model.class)
            .register(OSDP_MONITORING_ACTIVITY_SHORT, MonitoringActivity.class)
            .register(OSDP_MONITORING_FACILITY_SHORT, MonitoringFacility.class)
            .register(OSDP_MONITORING_PROGRAMME_SHORT, MonitoringProgramme.class)
            .register(OSDP_PUBLICATION_SHORT, Publication.class)
            .register(OSDP_SAMPLE_SHORT, Sample.class)
            .register(ERAMMP_MODEL_SHORT, ErammpModel.class)
            .register(NERC_MODEL, NercModel.class)
            .register(NERC_MODEL_USE, NercModelUse.class)
            .register(ERAMMP_DATACUBE_SHORT, ErammpDatacube.class)
            .register(SAMPLE_ARCHIVE_SHORT, SampleArchive.class)
            .register(DATA_TYPE_SHORT, DataType.class)
            .register(UKEMS_DOCUMENT_SHORT, UkemsDocument.class);
    }

    @Bean
    public GetCapabilitiesObtainerService getCapabilitiesObtainerService(
        MapServerDetailsService mapServerDetailsService
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Collections.singletonList(
            new Xml2WmsCapabilitiesMessageConverter()
        ));
        return new GetCapabilitiesObtainerService(restTemplate, mapServerDetailsService);
    }

    @Bean
    public DocumentRepository documentRepository(
        BundledReaderService<MetadataDocument> bundledReaderService,
        DocumentIdentifierService documentIdentifierService,
        DocumentReadingService documentReadingService,
        DocumentTypeLookupService documentTypeLookupService,
        DocumentWritingService documentWritingService,
        GitRepoWrapper gitRepoWrapper
    ) {
        return new GitDocumentRepository(
            documentTypeLookupService,
            documentReadingService,
            documentIdentifierService,
            documentWritingService,
            bundledReaderService,
            gitRepoWrapper
        );
    }

    @Bean
    public VocabularyService vocabularyService(
        @Qualifier("sparql") RestTemplate restTemplate,
        @Value("${sparql.endpoint}") String sparqlEndpoint,
        @Value("${sparql.graph}") String sparqlGraph
    ) {
        return new SparqlVocabularyService(new SparqlVocabularyRetriever(restTemplate, sparqlEndpoint, sparqlGraph).retrieve());
    }

    @Bean
    @SuppressWarnings("UnstableApiUsage") // Because EventBus is still @Beta!
    public EventBus communicationBus() {
        return new EventBus();
    }

    @Bean
    public AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory() {
        return new AnnotatedUserHelper<>(CatalogueUser.class);
    }

    @Bean
    @SuppressWarnings("UnstableApiUsage")
    @SneakyThrows
    public DataRepository<CatalogueUser> dataRepository(
        @Value("${data.repository.location}") String dataRepositoryLocation,
        AnnotatedUserHelper<CatalogueUser> annotatedUserHelper,
        EventBus eventBus
    ) {
        return new GitDataRepository<>(
            new File(dataRepositoryLocation),
            new InMemoryUserStore<>(),
            annotatedUserHelper,
            eventBus
        );
    }

    @Bean
    public DocumentInfoMapper<MetadataInfo> documentInfoMapper(
        ObjectMapper objectMapper
    ) {
        return new JacksonDocumentInfoMapper<>(objectMapper, MetadataInfo.class);
    }

    @Bean(destroyMethod = "close")
    public org.apache.jena.query.Dataset tdbModel(
        @Value("${jena.location}") String location
    ) {
        log.info("Creating Dataset at: {}", location);
        return TDBFactory.createDataset(location);
    }

    @Bean
    public SolrClient solrClient(
        @Value("${solr.server.url}") String solrDocumentServerUrl
    ){
        return new HttpSolrClient.Builder(solrDocumentServerUrl).build();
    }

    @Bean
    DocumentWritingService documentWritingService(
        List<HttpMessageConverter<?>> messageConverters
    ) {
        if (log.isDebugEnabled()) {
            messageConverters.forEach(converter -> log.debug(converter.toString()));
        }
        return new MessageConverterWritingService(messageConverters);
    }
}
