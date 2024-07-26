package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.tdb.TDBFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.indexing.ClassMap;
import uk.ac.ceh.gateway.catalogue.indexing.PrioritisedClassMap;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.method.MethodRecord;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModel;
import uk.ac.ceh.gateway.catalogue.modelnerc.NercModelUse;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.osdp.Agent;
import uk.ac.ceh.gateway.catalogue.osdp.Publication;
import uk.ac.ceh.gateway.catalogue.osdp.Sample;
import uk.ac.ceh.gateway.catalogue.postprocess.BaseMonitoringTypePostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.ClassMapPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.GeminiDocumentPostProcessingService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapper;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.ukems.UkemsDocument;
import uk.ac.ceh.gateway.catalogue.wms.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.wms.MapServerGetFeatureInfoErrorHandler;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.DocumentTypes.*;

@Slf4j
@Configuration
public class ServicesConfig {

    @Bean
    @Qualifier("normal")
    public RestTemplate normalRestTemplate() {
        log.info("Creating Normal RestTemplate");
        return new RestTemplate();
    }

    @Bean
    @Qualifier("sparql")
    public RestTemplate sparqlRestTemplate(ObjectMapper objectMapper) {
        log.info("Creating SPARQL RestTemplate");
        val messageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        val supportedMediaTypes = Arrays.asList(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "*+json")
        );
        messageConverter.setSupportedMediaTypes(supportedMediaTypes);
        val restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(List.of(
            messageConverter
        ));
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
        org.apache.jena.query.Dataset tdbModel
    ) {
        ClassMap<PostProcessingService> mappings = new PrioritisedClassMap<PostProcessingService>()
            .register(GeminiDocument.class, new GeminiDocumentPostProcessingService(citationService, dataciteService))
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
            .register(CEH_MODEL, CehModel.class)
            .register(CEH_MODEL_APPLICATION, CehModelApplication.class)
            .register(CODE, CodeDocument.class)
            .register(DATA_TYPE, DataType.class)
            .register(EF_DOCUMENT, BaseMonitoringType.class)
            .register(ERAMMP_DATACUBE, ErammpDatacube.class)
            .register(INFRASTRUCTURERECORD, InfrastructureRecord.class)
            .register(METHODRECORD, MethodRecord.class)
            .register(ELTER, ElterDocument.class)
            .register(ERAMMP_MODEL, ErammpModel.class)
            .register(GEMINI, GeminiDocument.class)
            .register(IMP, ImpDocument.class)
            .register(LINK, LinkDocument.class)
            .register(NERC_MODEL, NercModel.class)
            .register(NERC_MODEL_USE, NercModelUse.class)
            .register(OSDP_AGENT, Agent.class)
            .register(OSDP_DATASET, uk.ac.ceh.gateway.catalogue.osdp.Dataset.class)
            .register(OSDP_MODEL, uk.ac.ceh.gateway.catalogue.osdp.Model.class)
            .register(MONITORING_ACTIVITY, MonitoringActivity.class)
            .register(MONITORING_FACILITY, MonitoringFacility.class)
            .register(MONITORING_NETWORK, MonitoringNetwork.class)
            .register(MONITORING_PROGRAMME, MonitoringProgramme.class)
            .register(OSDP_PUBLICATION, Publication.class)
            .register(OSDP_SAMPLE, Sample.class)
            .register(SAMPLE_ARCHIVE, SampleArchive.class)
            .register(SERVICE_AGREEMENT, ServiceAgreement.class)
            .register(UKEMS_DOCUMENT, UkemsDocument.class);
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
    public VocabularyService vocabularyService() {
        return (broader, keyword) -> false;
    }

    @Bean
    @SuppressWarnings("UnstableApiUsage") // Because EventBus is still @Beta!
    public EventBus communicationBus() {
        return new EventBus();
    }

    @Bean
    @SneakyThrows
    public DataRepository<CatalogueUser> dataRepository(
        @Value("${data.repository.location}") String dataRepositoryLocation,
        EventBus eventBus
    ) {
        return new GitDataRepository<>(
            new File(dataRepositoryLocation),
            new InMemoryUserStore<>(),
            new AnnotatedUserHelper<>(CatalogueUser.class),
            eventBus
        );
    }

    @Bean
    public DocumentInfoMapper<MetadataInfo> metadataInfoMapper(
        ObjectMapper objectMapper
    ) {
        return new JacksonDocumentInfoMapper<>(objectMapper, MetadataInfo.class);
    }

    @Bean
    public DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper(
        ObjectMapper objectMapper
    ) {
        return new JacksonDocumentInfoMapper<>(objectMapper, ServiceAgreement.class);
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
        @Value("${solr.server.url}") String solrServerUrl
    ){
        return new HttpJdkSolrClient.Builder(solrServerUrl).build();
    }
}
