package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.quality.MetadataCheck;
import uk.ac.ceh.gateway.catalogue.quality.Results;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.ERROR;

@ExtendWith(MockitoExtension.class)
class ServiceAgreementModelAssemblerTest {
    @Mock private DocumentRepository documentRepository;

    @Mock private ServiceAgreementQualityService serviceAgreementQualityService;

    private ServiceAgreementModelAssembler assembler;

    private final String id = "b7fc9ed3-c166-45ec-93a9-93a294ab74a9";

    @BeforeEach
    void setup() {
        assembler = new ServiceAgreementModelAssembler(documentRepository, serviceAgreementQualityService);
    }

    @Test
    void publishLinkAppears() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("pending publication").build());
        givenDraftGeminiDocument();

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertTrue(model.getLink("publish").isPresent());
    }

    @Test
    void publishLinkDoesNotAppear() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("draft").build());
        List<MetadataCheck> problems = new ArrayList<>();
        Results results = new Results(problems, id);
        given(serviceAgreementQualityService.check(serviceAgreement.getId())).willReturn(results);

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertFalse(model.getLink("publish").isPresent());
    }

    @Test
    void historyLinkAppears() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        List<MetadataCheck> problems = new ArrayList<>();
        Results results = new Results(problems, id);
        given(serviceAgreementQualityService.check(serviceAgreement.getId())).willReturn(results);

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertTrue(model.getLink("history").isPresent());
    }

    @Test
    void permissionLinkAppears() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("pending publication").build());
        givenDraftGeminiDocument();

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertTrue(model.getLink("add-editor").isPresent());
    }

    @Test
    void permissionLinkDoesNotAppear() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("draft").build());
        List<MetadataCheck> problems = new ArrayList<>();
        Results results = new Results(problems, id);
        given(serviceAgreementQualityService.check(serviceAgreement.getId())).willReturn(results);

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertFalse(model.getLink("add-editor").isPresent());
    }

    @Test
    void submitLinkAppears() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("draft").build());
        List<MetadataCheck> problems = new ArrayList<>();
        Results results = new Results(problems, id);
        given(serviceAgreementQualityService.check(serviceAgreement.getId())).willReturn(results);

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertTrue(model.getLink("submit").isPresent());
    }

    @Test
    void submitLinkDoesNotAppear() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("published").build());

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertFalse(model.getLink("submit").isPresent());
    }

    @Test
    void submitLinkDoesNotAppearErrors() {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("draft").build());
        List<MetadataCheck> problems = new ArrayList<>();
        problems.add(new MetadataCheck("test", ERROR));
        Results results = new Results(problems, id);
        given(serviceAgreementQualityService.check(serviceAgreement.getId())).willReturn(results);

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertFalse(model.getLink("submit").isPresent());
    }

    @SneakyThrows
    private void givenDraftGeminiDocument() {
        val geminiDocument = new GeminiDocument();
        geminiDocument.setMetadata(MetadataInfo.builder().state("draft").build());
        given(documentRepository.read(id))
            .willReturn(geminiDocument);
    }

}
