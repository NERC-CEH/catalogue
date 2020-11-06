package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.sparql.VocabularyService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolrIndexMetadataDocumentGeneratorTest {

    @Mock CodeLookupService codeLookupService;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock VocabularyService vocabularyService;
    private SolrIndexMetadataDocumentGenerator generator;
    
    @Before
    public void createGeminiDocumentSolrIndexGenerator() {
        generator = new SolrIndexMetadataDocumentGenerator(
            codeLookupService,
            documentIdentifierService,
            vocabularyService
        );
    }

    @Test
    @SneakyThrows
    public void boundingBoxAndGeometryLocationsAddedToIndex() {
        //Given
        MonitoringFacility document = new MonitoringFacility();
        document.setBoundingBox(BoundingBox.builder()
            .northBoundLatitude("59.4")
            .eastBoundLongitude("2.4")
            .southBoundLatitude("53.3")
            .westBoundLongitude("-0.5")
            .build()
        );
        document.setGeometry("POLYGON((30 10, 40 40, 20 40, 10 20, 30 10))");

        //When
        SolrIndex actual = generator.generateIndex(document);

        //Then
        assertThat("locations transferred to index", actual.getLocations(), hasItems("POLYGON((30 10, 40 40, 20 40, 10 20, 30 10))", "POLYGON((-0.5 53.3, -0.5 59.4, 2.4 59.4, 2.4 53.3, -0.5 53.3))"));
    }

    @Test
    @SneakyThrows
    public void geometryLocationsAddedToIndex() {
        //Given
        MonitoringFacility document = new MonitoringFacility();
        document.setGeometry("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))");

        //When
        SolrIndex actual = generator.generateIndex(document);

        //Then
        assertThat("locations transferred to index", actual.getLocations(), hasItems("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))"));
    }

    @Test
    @SneakyThrows
    public void boundingBoxLocationsAddedToIndex() {
        //Given
        MonitoringFacility document = new MonitoringFacility();
        document.setBoundingBox(BoundingBox.builder()
            .northBoundLatitude("59.4")
            .eastBoundLongitude("2.4")
            .southBoundLatitude("53.3")
            .westBoundLongitude("-0.5")
            .build()
        );

        //When
        SolrIndex actual = generator.generateIndex(document);

        //Then
        assertThat("locations transferred to index", actual.getLocations(), hasItems("POLYGON((-0.5 53.3, -0.5 59.4, 2.4 59.4, 2.4 53.3, -0.5 53.3))"));
    }

    @Test
    public void applicationScaleAddedToIndex() throws Exception {
        //Given
        Model document = new Model();
        document.setApplicationScale("global");
        
        //When
        SolrIndex actual = generator.generateIndex(document);
        
        //Then
        assertThat("applicationScale transferred to index", actual.getImpScale(), contains("global"));
    }
    
    @Test
    public void scaleAddedFromModel() throws Exception {
        //Given
        CehModel model = new CehModel();
        model.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/global").value("global").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/catchment").value("catchment").build()
        ));
        
        //When
        SolrIndex index = generator.generateIndex(model);
        List<String> actual = index.getImpScale();
        
        //Then
        assertThat("Solr index should have model scale", actual, contains("global", "catchment"));
    }
    
    @Test
    public void scaleAddedFromModelApplication() throws Exception {
        //Given
        CehModelApplication application = new CehModelApplication();
        application.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/global").value("global").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/catchment").value("catchment").build()
        ));
        CehModelApplication.ModelInfo info0 = new CehModelApplication.ModelInfo();
        info0.setSpatialExtentOfApplication("plot");
        CehModelApplication.ModelInfo info1 = new CehModelApplication.ModelInfo();
        application.setModelInfos(Arrays.asList(info0, info1));
        
        //When
        SolrIndex index = generator.generateIndex(application);
        List<String> actual = index.getImpScale();
        
        //Then
        assertThat("Solr index should have model application scale", actual, contains("global", "catchment", "plot"));
    }
    
    @Test
    public void topicAddedFromModelApplication() throws Exception {
        //Given
        CehModelApplication application = new CehModelApplication();
        application.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/topic/nitrogen").value("nitrogen").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/topic/management").value("management").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/plot").value("plot").build()
        ));
        
        //When
        SolrIndex index = generator.generateIndex(application);
        List<String> actual = index.getImpTopic();
        
        //Then
        assertThat("Solr index should have model application topic", actual, contains("nitrogen", "management"));
    }
    
    @Test
    public void checkThatTitleIsTransferedToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setTitle("my gemini document");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my title", "my gemini document", index.getTitle());
    }
    
    @Test
    public void checkThatIdTransferedToIndex() {
        //Given
        String id = "some crazy long, hard to rememember, number";
        when(documentIdentifierService.generateFileId(id)).thenReturn("myid");
        GeminiDocument document = new GeminiDocument();
        document.setId(id);
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my id", "myid", index.getIdentifier());
    }

    @Test
    public void checkThatElterUriTransferedToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        List<Keyword> keywords = new ArrayList<>();
        Keyword keyword = Keyword.builder().URI("https://deims.org/266eedce-b67c-4935-a7b7-4dc3c169c902")
                .value("Aamotsdalen - Norway").build();
        keywords.add(keyword);
        DescriptiveKeywords descriptiveKeywords = DescriptiveKeywords.builder().keywords(keywords).build();
        List<DescriptiveKeywords> descriptiveKeywordsList = new ArrayList<>();
        descriptiveKeywordsList.add(descriptiveKeywords);
        document.setDescriptiveKeywords(descriptiveKeywordsList);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals("Expected to get my URI", "https://deims.org/266eedce-b67c-4935-a7b7-4dc3c169c902", index.getElterDeimsUri().get(0));
    }

    @Test
    public void checkThatElterSiteTransferedToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        List<Keyword> keywords = new ArrayList<>();
        Keyword keyword = Keyword.builder().URI("https://deims.org/266eedce-b67c-4935-a7b7-4dc3c169c902")
                .value("Aamotsdalen - Norway").build();
        keywords.add(keyword);
        DescriptiveKeywords descriptiveKeywords = DescriptiveKeywords.builder().keywords(keywords).build();
        List<DescriptiveKeywords> descriptiveKeywordsList = new ArrayList<>();
        descriptiveKeywordsList.add(descriptiveKeywords);
        document.setDescriptiveKeywords(descriptiveKeywordsList);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals("Expected to get my URI", "Aamotsdalen - Norway", index.getElterDeimsSite().get(0));
    }
    
    @Test
    public void checkThatDescriptionIsTransferedToIndex() {
        //Given
        String description = "Once upon a time, there was a metadata record...";
        GeminiDocument document = new GeminiDocument();
        document.setDescription(description);        
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my description", description, index.getDescription());
    }
    
    @Test
    public void checkThatResourceTypeIsTransferedToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setResourceType(Keyword.builder().value("dataset").build());
        when(codeLookupService.lookup("metadata.resourceType", "dataset")).thenReturn("Dataset");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my resourceType", "Dataset", index.getResourceType());
    }
    
    @Test
    public void checkThatLongDescriptionWithSpacesIsShortened(){
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatLongDescriptionWithoutSpacesIsShortened(){
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);

        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatShortDescriptionIsNotShortened(){
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "I am short";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be shorter than the threshold length of " + maxDescriptionLength, maxDescriptionLength, greaterThan(description.length()));
        assertEquals("Shortened description is the same length as the original description", description.length(), document.getShortenedDescription().length());
    }
    
    @Test
    public void checkNullDescriptionGeneratesEmptyStringForShortenedDescription(){
        //Given
        SolrIndex document = new SolrIndex();
        document.setDescription(null);
        
        //When
        String expected = document.getShortenedDescription();
        
        //Then
        assertThat("Expected shortenedDescription to be empty string" , expected, equalTo(""));
    }
    
    @Test
    public void checkThatCatalogueIsTransferedToIndex() {
        //Given
        MetadataInfo info = MetadataInfo.builder().catalogue("eidc").build();
        MetadataDocument document = new GeminiDocument().setMetadata(info);
        
        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(
                "Expected to get 'eidc'",
                index.getCatalogue(),
                equalTo("eidc")
        );
    }

    @Test
    public void checkThatVocabularyServiceUsed() {
        //Given
        DescriptiveKeywords ncterms = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("farm")
                    .URI("http://vocabs.ceh.ac.uk/ncterms/farm")
                    .build(),
                Keyword.builder()
                    .value("field")
                    .URI("http://vocabs.ceh.ac.uk/ncterms/field")
                    .build()
                )
            ).build();

        DescriptiveKeywords other = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Green")
                    .build(),
                Keyword.builder()
                    .value("Blue")
                    .URI("http://example.com/blue")
                    .build()
                )
            ).build();

        GeminiDocument document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(ncterms, other));


        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        verify(vocabularyService).isMember(
            "http://vocabs.ceh.ac.uk/ncterms/geographical_scale",
            "http://vocabs.ceh.ac.uk/ncterms/farm"
        );
        verify(vocabularyService).isMember(
            "http://vocabs.ceh.ac.uk/ncterms/geographical_scale",
            "http://vocabs.ceh.ac.uk/ncterms/field"
        );
        verify(vocabularyService).isMember(
            "http://vocabs.ceh.ac.uk/ncterms/geographical_scale",
            "http://example.com/blue"
        );
    }
    
    @Test
    public void checkThatImpScaleIsIndexed() {
        //Given
        DescriptiveKeywords imp = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Catchment")
                    .URI("http://vocabs.ceh.ac.uk/imp/scale/catchment")
                    .build(),
                Keyword.builder()
                    .value("National")
                    .URI("http://vocabs.ceh.ac.uk/imp/scale/national")
                    .build()
            )
        ).build();
        
        DescriptiveKeywords other = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Green")
                    .build(),
                Keyword.builder()
                    .value("Blue")
                    .URI("http://example.com/blue")
                    .build()
            )
        ).build();
        
        GeminiDocument document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(imp, other));
            
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected catchment and national indexed", index.getImpScale(), contains("Catchment", "National"));
        assertThat("Expected to not index Blue", index.getImpScale(), not(contains("Blue")));
        
    }
    
    @Test
    public void checkThatImpCaMMPIssuesIsIndexed() {
        //Given
        DescriptiveKeywords imp = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Agri-environment")
                    .URI("http://vocabs.ceh.ac.uk/imp/ci/agri-environment")
                    .build(),
                Keyword.builder()
                    .value("Ecosystem Response")
                    .URI("http://vocabs.ceh.ac.uk/imp/ci/ecosystem-response")
                    .build()
            )
        ).build();
        
        DescriptiveKeywords other = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Green")
                    .build(),
                Keyword.builder()
                    .value("Blue")
                    .URI("http://example.com/blue")
                    .build()
            )
        ).build();
        
        GeminiDocument document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(imp, other));
            
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected agri-environment and ecosytem response indexed", index.getImpCaMMPIssues(), contains("Agri-environment", "Ecosystem Response"));
        assertThat("Expected to not index Blue", index.getImpScale(), not(contains("Blue")));
        
    }
    
    @Test
    public void checkThatImpWaterQualityIsIndexed() {
        //Given
        DescriptiveKeywords imp = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Nitrogen")
                    .URI("http://vocabs.ceh.ac.uk/imp/wp/nitrogen")
                    .build(),
                Keyword.builder()
                    .value("Phosphorous")
                    .URI("http://vocabs.ceh.ac.uk/imp/wp/phosphorous")
                    .build()
            )
        ).build();
        
        DescriptiveKeywords other = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Green")
                    .build(),
                Keyword.builder()
                    .value("Blue")
                    .URI("http://example.com/blue")
                    .build()
            )
        ).build();
        
        GeminiDocument document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(imp, other));
            
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected nitrogen and phosphorous to be indexed", index.getImpWaterPollutant(), contains("Nitrogen", "Phosphorous"));
        assertThat("Expected to not index Blue", index.getImpWaterPollutant(), not(contains("Blue")));
        
    }

}
