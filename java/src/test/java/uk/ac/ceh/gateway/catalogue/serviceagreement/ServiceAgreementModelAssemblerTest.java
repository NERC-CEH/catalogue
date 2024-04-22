package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
    void setup(TestInfo testInfo) {
        assembler = new ServiceAgreementModelAssembler(documentRepository, serviceAgreementQualityService, String.join(",", testInfo.getTags()));
    }

    void publishLinkAppears(String schema) {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("pending publication").build());
        givenDraftGeminiDocument();

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        String relation = "publish";
        assertTrue(model.getLink(relation).isPresent());
        assertTrue(model.getLink(relation).get().getHref().startsWith(schema));
    }

    @Test
    @Tag("prod")
    void publishLinkAppearsProd() {
        publishLinkAppears("https:");
    }

    @Test
    @Tag("development")
    void publishLinkAppearsDev() {
        publishLinkAppears("http:");
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

    void historyLinkAppears(String schema) {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        List<MetadataCheck> problems = new ArrayList<>();
        Results results = new Results(problems, id);
        given(serviceAgreementQualityService.check(serviceAgreement.getId())).willReturn(results);

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        String relation = "history";
        assertTrue(model.getLink(relation).isPresent());
        assertTrue(model.getLink(relation).get().getHref().startsWith(schema));
    }

    @Test
    @Tag("prod")
    void historyLinkAppearsProd() {
        publishLinkAppears("https:");
    }

    @Test
    @Tag("development")
    void historyLinkAppearsDev() {
        publishLinkAppears("http:");
    }

    void permissionLinkAppears(String schema) {
        //given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        serviceAgreement.setMetadata(MetadataInfo.builder().state("pending publication").build());
        givenDraftGeminiDocument();

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        String relation = "add-editor";
        assertTrue(model.getLink(relation).isPresent());
        assertTrue(model.getLink(relation).get().getHref().startsWith(schema));
    }

    @Test
    @Tag("prod")
    void permissionLinkAppearsProd() {
        publishLinkAppears("https:");
    }

    @Test
    @Tag("development")
    void permissionLinkAppearsDev() {
        publishLinkAppears("http:");
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

    void submitLinkAppears(String schema) {
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
        String relation = "submit";
        assertTrue(model.getLink(relation).isPresent());
        assertTrue(model.getLink(relation).get().getHref().startsWith(schema));
    }

    @Test
    @Tag("prod")
    void submitLinkAppearsProd() {
        publishLinkAppears("https:");
    }

    @Test
    @Tag("development")
    void submitLinkAppearsDev() {
        publishLinkAppears("http:");
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
