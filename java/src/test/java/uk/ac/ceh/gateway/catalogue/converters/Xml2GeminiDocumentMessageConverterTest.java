package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import java.time.LocalDate;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DownloadOrder;
import uk.ac.ceh.gateway.catalogue.gemini.elements.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResponsibleParty.Address;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ThesaurusName;
import uk.ac.ceh.gateway.catalogue.gemini.elements.TimePeriod;
/**
 *
 * @author cjohn
 */
public class Xml2GeminiDocumentMessageConverterTest {
    private Xml2GeminiDocumentMessageConverter geminiReader;
    
    @Before
    public void createGeminiDocumentConverter() throws XPathExpressionException {
        geminiReader = new Xml2GeminiDocumentMessageConverter();
    }
    
    @Test
    public void canGetResponsibleParty() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("responsibleParty.xml"));
        List<ResponsibleParty> expected = Arrays.asList(
            ResponsibleParty.builder()
                .organisationName("Centre for Ecology & Hydrology")
                .role("pointOfContact")
                .email("enquiries@ceh.ac.uk")
                .address(Address.builder()
                    .deliveryPoint("Maclean Building, Benson Lane, Crowmarsh Gifford")
                    .city("Wallingford")
                    .administrativeArea("Oxfordshire")
                    .postalCode("OX10 8BB")
                    .country("UK")
                    .build()
                )
                .build(),
            ResponsibleParty.builder()
                .individualName("Reynolds,B.")
                .organisationName("Centre for Ecology & Hydrology")
                .role("author")
                .email("enquiries@ceh.ac.uk")
                .address(Address.builder()
                    .deliveryPoint("Environment Centre Wales, Deiniol Road")
                    .city("Bangor")
                    .administrativeArea("Gwynedd")
                    .postalCode("LL57 2UW")
                    .country("UK")
                    .build()
                )
                .build(),
            ResponsibleParty.builder()
                .individualName("Neal,C.")
                .organisationName("Centre for Ecology & Hydrology")
                .role("author")
                .email("enquiries@ceh.ac.uk")
                .address(Address.builder()
                    .deliveryPoint("Maclean Building, Benson Lane, Crowmarsh Gifford")
                    .city("Wallingford")
                    .administrativeArea("Oxfordshire")
                    .postalCode("OX10 8BB")
                    .country("UK")
                    .build()
                )
                .build(),
            ResponsibleParty.builder()
                .individualName("Kirchner,J.")
                .organisationName("University of California, Berkley")
                .role("author")
                .email("enquiries@ceh.ac.uk")
                .address(Address.builder().build())
                .build(),
            ResponsibleParty.builder()
                .individualName("Norris,D.")
                .organisationName("Centre for Ecology & Hydrology")
                .role("pointOfContact")
                .email("enquiries@ceh.ac.uk")
                .address(Address.builder()
                    .deliveryPoint("Environment Centre Wales, Deiniol Road")
                    .city("Bangor")
                    .administrativeArea("Gwynedd")
                    .postalCode("LL57 2UW")
                    .country("UK")
                    .build()
                )
                .build(),
            ResponsibleParty.builder()
                .organisationName("Centre for Ecology & Hydrology")
                .role("distributor")
                .email("enquiries@ceh.ac.uk")
                .address(Address.builder()
                    .deliveryPoint("Maclean Building, Benson Lane, Crowmarsh Gifford")
                    .city("Wallingford")
                    .administrativeArea("Oxfordshire")
                    .postalCode("OX10 8BB")
                    .country("UK")
                    .build()
                )
                .build()
        );
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<ResponsibleParty> actual = document.getResponsibleParties();
        
        //Then
        assertThat("ResponsibleParties 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canGetTimeExtent() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("timeExtent.xml"));
        List<TimePeriod> expected = Arrays.asList(
            new TimePeriod("1987-04-21", "1989-12-31"),
            new TimePeriod("1999-03-30", "2013-10-10"),
            new TimePeriod("2014-03-12", "")
        );
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<TimePeriod> actual = document.getTemporalExtent();
        
        //Then
        assertThat("TemporalExtent 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canGetOtherCitationDetailsFromDataset() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("otherCitationDetailsDataset.xml"));
        String expected = "This is other citation details";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        String actual = document.getOtherCitationDetails();
        
        //Then
        assertThat("OtherCitationDetails 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canGetResourceStatus() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("resourceStatus.xml"));
        String expected = "onGoing";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        String actual = document.getResourceStatus();
        
        //Then
        assertThat("resourceStatus 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canNotGetResourceStatus() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("browseGraphicUrl.xml"));
        String expected = "";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        String actual = document.getResourceStatus();
        
        //Then
        assertThat("resourceStatus 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canGetBrowseGraphicUrl() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("browseGraphicUrl.xml"));
        String expected = "https://gateway.ceh.ac.uk:443/smartEditor/preview/d481e451-9094-4983-aca9-46b170d840d8.png";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        String actual = document.getBrowseGraphicUrl();
        
        //Then
        assertThat("BrowseGraphicUrl 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canNotGetBrowseGraphicUrl() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("noBrowseGraphicUrl.xml"));
        String expected = "";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        String actual = document.getBrowseGraphicUrl();
        
        //Then
        assertThat("BrowseGraphicUrl 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canGetOtherCitationDetailsFromService() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("otherCitationDetailsService.xml"));
        String expected = "This is other citation details - service";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        String actual = document.getOtherCitationDetails();
        
        //Then
        assertThat("OtherCitationDetails 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void otherCitationDetailsFromEmptyElementIsNotNull() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("otherCitationDetailsServiceEmpty.xml"));
        String expected = "";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        String actual = document.getOtherCitationDetails();
        
        //Then
        assertThat("OtherCitationDetails should not be null", actual, notNullValue());
        assertThat("OtherCitationDetails 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void canGetNonOglDownloadOrder() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("nonOglDownloadOrder.xml"));
        DownloadOrder expected = DownloadOrder
            .builder()
            .orderUrl("http://gateway.ceh.ac.uk/download?fileIdentifier=11caad35-4a33-4ad8-852b-6c120fd250e2")
            .supportingDocumentsUrl("http://eidchub.ceh.ac.uk/metadata/11caad35-4a33-4ad8-852b-6c120fd250e2")
            .licenseUrl("http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/standard-click-through/plain")
            .build();
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        DownloadOrder actual = document.getDownloadOrder();
        
        //Then
        assertThat("DownloadOrder 'actual' should be equal to 'expected'", actual, equalTo(expected));
        assertThat("OGL license should be false", actual.isOgl(), equalTo(false));
    }
    
    @Test
    public void canGetOglDownloadOrder() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("oglDownloadOrder.xml"));
        DownloadOrder expected = DownloadOrder
            .builder()
            .orderUrl("http://gateway.ceh.ac.uk/download?fileIdentifier=11caad35-4a33-4ad8-852b-6c120fd250e2")
            .supportingDocumentsUrl("http://eidchub.ceh.ac.uk/metadata/11caad35-4a33-4ad8-852b-6c120fd250e2")
            .licenseUrl("http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
            .build();
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        DownloadOrder actual = document.getDownloadOrder();
        
        //Then
        assertThat("DownloadOrder 'actual' should be equal to 'expected'", actual, equalTo(expected));
        assertThat("OGL license should be false", actual.isOgl(), equalTo(true));
    }
    
    @Test
    public void canGetBoundingBox() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("multipleSpatialExtent.xml"));
        List<BoundingBox> expected = Arrays.asList(
            BoundingBox.builder()
                .westBoundLongitude("-9.0939")
                .eastBoundLongitude("3.2549")
                .southBoundLatitude("49.6642")
                .northBoundLatitude("61.0363")
                .build(),
            BoundingBox.builder()
                .westBoundLongitude("12.0424")
                .eastBoundLongitude("36.0000")
                .southBoundLatitude("68.1298")
                .northBoundLatitude("74.2389")
                .build()
        );
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<BoundingBox> actual = document.getBoundingBoxes();
        
        //Then
        assertThat("BoundingBoxes 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }

    @Test
    public void canGetId() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("id.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertEquals("Expected to be able to read the id", "9e7790ab-a37d-4918-8107-5c427798ca68", document.getId());
        assertFalse("Expected id to not be empty string", document.getId().isEmpty());
    }

    @Test
    public void canGetTitle() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "title.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertNotNull("Expected title to have content", document.getTitle());
        assertFalse("Expected title to not be empty string", document.getTitle().isEmpty());
    }
    
    @Test
    public void canGetAlternateTitles() throws IOException {
       
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "alternateTitles.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<String> actual = document.getAlternateTitles();
        List<String> expected = Arrays.asList("First alternate title", "Second alternate title");
        Collections.sort(actual);
        Collections.sort(expected);
        
        //Then
        assertNotNull("Expected title to have content", actual);
        assertThat("Content of alternateTitles not as expected", actual, is(expected));
    }
    
    @Test
    public void canGetDatasetLanguage() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("language.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        CodeListItem actual = document.getDatasetLanguage();
        CodeListItem expected = CodeListItem
                .builder()
                .codeList("http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#LanguageCode")
                .value("eng")
                .build();
        
        //Then
        assertNotNull("Expected language type not to be null", actual);
        assertNotNull("Expected language code not to be null", actual.getValue());
        assertNotNull("Expected language code list to not be null", actual.getCodeList());
        assertFalse("Expected language code not be empty string", actual.getValue().isEmpty());
        assertFalse("Expected language code list not be empty string", actual.getCodeList().isEmpty());
        assertEquals("Language not as expected", "eng", actual.getValue());
        assertEquals("Codelist not as expected", "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#LanguageCode", actual.getCodeList());
        assertEquals("Content of DatasetLanguage not as expected", expected, actual);
    }
    
    @Test
    public void canGetCoupledResources() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("coupledResources.xml"));
        List<String> expected = Arrays.asList("CEH:EIDC:#1275577974562", "CEH:EIDC:#9984234423443");
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<String> actual = document.getCoupleResources();
        
        //Then
        assertThat("CoupledResources 'actual' should be equal to 'expected'", actual, equalTo(expected));   
    }
    
    @Test
    public void canGetTopicCategories() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("topicCategories.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<String> actual = document.getTopicCategories();
        List<String> expected = Arrays.asList("environment", "imageryBaseMapsEarthCover");
        Collections.sort(actual);
        Collections.sort(expected);
        
        //Then
        assertNotNull("Expected TopicCategories to not be null", actual);
        assertThat("Content of TopicCategories is not as expected", actual, is(expected));
    }
    
    @Test
    public void canGetUncitedKeywords() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsUncited.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected DescriptiveKeywords not to be null", descriptiveKeywords);
        assertThat("DescriptiveKeywords list should have 1 DescriptiveKeywords entry", descriptiveKeywords.size(), is(1));
        
        //When
        List<Keyword> actual = descriptiveKeywords.get(0).getKeywords();
        List<Keyword> expected = Arrays.asList(
            Keyword
                .builder()
                .value("Uncited 1")
                .URI(null)
                .build()
            ,Keyword
                .builder()
                .value("Uncited 2")
                .URI(null)
                .build()
        );
        
        //Then
        assertNotNull("Expected Keywords to not be null", actual);
        assertThat("Content of Keywords is not as expected", actual, is(expected));
    }
    
    @Test
    public void canGetCitedKeywords() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsCited.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected DescriptiveKeywords not to be null", descriptiveKeywords);
        assertThat("DescriptiveKeywords list should have 2 DescriptiveKeywords entries", descriptiveKeywords.size(), is(2));
    }
    
    @Test
    public void canGetCitedKeywordsSimple() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsCitedSimple.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected DescriptiveKeywords not to be null", descriptiveKeywords);
        
        //When
        List<Keyword> actualKeywords = descriptiveKeywords.get(0).getKeywords();
        List<Keyword> expectedKeywords = Arrays.asList(
            Keyword
                .builder()
                .value("Cited Simple 1")
                .URI(null)
                .build()
            ,Keyword
                .builder()
                .value("Cited Simple 2")
                .URI(null)
                .build()
        );
        
        //Then
        assertNotNull("Expected Keywords to not be null", actualKeywords);
        assertThat("Content of Keywords is not as expected", actualKeywords, is(expectedKeywords));
    }
    
    @Test
    public void canGetCitedKeywordsLinks() throws IOException{
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsCitedLinks.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected DescriptiveKeywords not to be null", descriptiveKeywords);
        
        //When
        List<Keyword> actualKeywords = descriptiveKeywords.get(0).getKeywords();
        List<Keyword> expectedKeywords = Arrays.asList(
            Keyword
                .builder()
                .value("links 1")
                .URI("http://www.google.com")
                .build()
            ,Keyword
                .builder()
                .value("links 2")
                .URI("http://www.bing.com")
                .build()
        );
        
        //Then
        assertNotNull("Expected Keywords to not be null", actualKeywords);
        assertThat("Content of Keywrods is not as expected", actualKeywords, is(expectedKeywords));
    }
    
    @Test
    public void canGetCitedKeywordsMixed() throws IOException{
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsCitedMixed.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected DescriptiveKeywords not to be null", descriptiveKeywords);
        
        //When
        List<Keyword> actualKeywords = descriptiveKeywords.get(0).getKeywords();
        List<Keyword> expectedKeywords = Arrays.asList(
            Keyword
                .builder()
                .value("Cited Simple 1")
                .URI(null)
                .build()
            ,Keyword
                .builder()
                .value("links 1")
                .URI("http://www.google.com")
                .build()
            ,Keyword
                .builder()
                .value("links 2")
                .URI("http://www.bing.com")
                .build()
        );
        
        //Then
        assertNotNull("Expected Keywords to not be null", actualKeywords);
        assertThat("Content of Keywrods is not as expected", actualKeywords, is(expectedKeywords));
    }
    
    @Test
    public void canGetCitedKeywordsType() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsCited.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected DescriptiveKeywords not to be null", descriptiveKeywords);

        //When
        CodeListItem actualType = descriptiveKeywords.get(0).getType();
        CodeListItem expectedType = CodeListItem
                .builder()
                .codeList("http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_KeywordTypeCode")
                .value("theme")
                .build();
        
        //Then
        assertNotNull("Expected Type to not be null", actualType);
        assertThat("Content of Type not as expected", actualType, is(expectedType));
    }
    
    @Test
    public void canGetCitedKeywordsThesaurus() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("keywordsCited.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        List<DescriptiveKeywords> descriptiveKeywords = document.getDescriptiveKeywords();
        
        //Then
        assertNotNull("Expected DescriptiveKeywords not to be null", descriptiveKeywords);

        //When
        ThesaurusName thesaurusName = descriptiveKeywords.get(0).getThesaurusName();
        
        //Then
        assertNotNull("Expected Thesaurus to not be null", thesaurusName);
        assertEquals("Title not as expected", "test thesaurus" , thesaurusName.getTitle());
        assertEquals("Date not as expected", LocalDate.of(2014, 6, 3), thesaurusName.getDate());
    }

    @Test
    public void canGetWhereXmlnsInline() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("languageInlineXmlns.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        CodeListItem actual = document.getDatasetLanguage();
        CodeListItem expected = CodeListItem
                .builder()
                .codeList("http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#LanguageCode")
                .value("eng")
                .build();
        
        //Then
        assertEquals("Content of DatasetLanguage not as expected", expected, actual);
    }

    @Test
    public void canGetDescription() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream(
                "description.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        String expectedTitle = "Test description text";
        assertNotNull("Expected description to have content", document.getDescription());
        assertFalse("Expected title to not be empty string", document.getDescription().isEmpty());
        assertEquals(String.format("Expected title to say'%s'.", expectedTitle), expectedTitle, document.getDescription());
    }

    @Test
    public void canGetResourceType() throws IOException {
        
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("resourceType.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        CodeListItem actual = document.getResourceType();
        CodeListItem expected = CodeListItem
                .builder()
                .codeList("http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode")
                .value("dataset")
                .build();
        
        //Then
        assertNotNull("Expected resourceType not to be null", actual);
        assertNotNull("Expected resourceType value not to be null", actual.getValue());
        assertNotNull("Expected resourcetType code list to not be null", actual.getCodeList());
        assertFalse("Expected resourceType value to not be an empty string", actual.getValue().isEmpty());
        assertFalse("Expected resourceType code list not be empty string", actual.getCodeList().isEmpty());
        assertEquals("resourceType not as expected", expected.getValue(), actual.getValue());
        assertEquals("Codelist not as expected", expected.getCodeList(), actual.getCodeList());
        assertEquals("Content of resourceType not as expected", expected, actual);

    
    }
    
    @Test
    public void canGetResourceIdentifiers() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("resourceIdentifiers.xml"));
        Set<ResourceIdentifier> expected = new HashSet(Arrays.asList(
            ResourceIdentifier.builder().code("1374152631039").codeSpace("CEH:EIDC:").build(),
            ResourceIdentifier.builder().code("10.5285/05e5d538-6be7-476d-9141-76d9328738a4").codeSpace("doi:").build(),
            ResourceIdentifier.builder().code("10/nt9").codeSpace("doi:").build()
        ));
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //When
        Set<ResourceIdentifier> actual = document.getResourceIdentifiers();
        
        //Then
        assertThat("actual resourceIdentifiers are equal to expected", actual, equalTo(expected));
    }
    
    @Test
    public void spatialReference() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("spatialReference.xml"));
        String expected = "OSGB 1936 / British National Grid";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);

        //Then
        assertThat("Actual title is as expected", document.getSpatialReferenceSystem().getTitle(), equalTo(expected));
    }
    
    @Test
    public void spatialReferenceDefaultTitle() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("spatialReferenceUnknown.xml"));
        String expected = "";
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);

        //Then
        assertThat("Actual title is as expected", document.getSpatialReferenceSystem().getTitle(), equalTo(expected));
    }
    
    @Test
    public void datasetReferenceDatesPublication() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("datasetReferenceDatesPublication.xml"));
        DatasetReferenceDate expected = DatasetReferenceDate.builder()
                .publicationDate(LocalDate.parse("2011-04-08"))
                .build();
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertThat("MetadataDate is correct", document.getDatasetReferenceDate(), equalTo(expected));
    }
    
    @Test
    public void datasetReferenceDatesAll() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("datasetReferenceDatesAll.xml"));
        DatasetReferenceDate expected = DatasetReferenceDate.builder()
                .creationDate(LocalDate.parse("2011-04-08"))
                .publicationDate(LocalDate.parse("2011-05-08"))
                .revisionDate(LocalDate.parse("2011-06-08"))
                .build();
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertThat("MetadataDate is correct", document.getDatasetReferenceDate(), equalTo(expected));
    }
    
    @Test
    public void metadataDate() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("metadataDate.xml"));
        LocalDate expected = LocalDate.parse("2012-10-15");
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);
        
        //Then
        assertThat("MetadataDate is correct", document.getMetadataDate(), equalTo(expected));
    }
    
    @Test
    public void spatialReferenceNull() throws IOException {
        //Given
        HttpInputMessage message = mock(HttpInputMessage.class);
        when(message.getBody()).thenReturn(getClass().getResourceAsStream("spatialReferenceMissing.xml"));
        
        //When
        GeminiDocument document = geminiReader.readInternal(GeminiDocument.class, message);

        //Then
        assertThat("Spatial reference is completely missing", document.getSpatialReferenceSystem(), is(nullValue()));
    }
}
