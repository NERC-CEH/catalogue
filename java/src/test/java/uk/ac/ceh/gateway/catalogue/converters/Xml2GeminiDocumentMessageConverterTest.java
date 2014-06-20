
package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.joda.time.LocalDate;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpInputMessage;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DownloadOrder;
import uk.ac.ceh.gateway.catalogue.gemini.elements.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ThesaurusName;
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
        assertEquals("Date not as expected", new LocalDate(2014, 6, 3), thesaurusName.getDate());
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

}
