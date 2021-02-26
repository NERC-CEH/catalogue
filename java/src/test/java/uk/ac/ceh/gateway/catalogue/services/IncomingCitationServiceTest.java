package uk.ac.ceh.gateway.catalogue.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Supplemental;
import uk.ac.ceh.gateway.catalogue.model.Citation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IncomingCitationServiceTest {

    public IncomingCitationService incomingCitationService;

    @Mock
    public Citation mockCitation;

    @Mock Supplemental supplemental;
    @Mock Supplemental isReferencedBy;
    @Mock Supplemental isSupplementTo;

    @Before
    public void init(){
        this.incomingCitationService = new IncomingCitationService();
    }
//Calculate incomingCitationCount as COUNT of supplemental
// WHERE type = 'isReferencedBy' OR type = 'isSupplementTo'
    @Test
    public void testGetIncomingCitationCount(){
        // Given
        GeminiDocument geminiDocument = new GeminiDocument();
        ReflectionTestUtils.setField(supplemental, "type", "test");
        ReflectionTestUtils.setField(isReferencedBy, "type", "isReferencedBy");
        ReflectionTestUtils.setField(isSupplementTo, "type", "isSupplementTo");
        List<Supplemental> supplementals = new ArrayList<>();
        supplementals.add(supplemental);
        supplementals.add(isReferencedBy);
        supplementals.add(isSupplementTo);
        geminiDocument.setSupplemental(supplementals);

        // When
        int output = incomingCitationService.getIncomingCitationCount(geminiDocument);

        // Then
        assertThat(output, is(1));
    }

}