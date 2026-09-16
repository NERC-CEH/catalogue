package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.quality.MetadataCheck;
import uk.ac.ceh.gateway.catalogue.quality.Results;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
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

    /**
     * The links these tests check used to take their scheme from the active Spring profiles, which
     * meant they were right only where someone had named a profile as expected, and left the port
     * alone - so production emitted {@code https://catalogue.ceh.ac.uk:443/...} (dri-one #260). They
     * come from the request now, so the request is what the test supplies: one already normalised by
     * {@code ForwardedHeaderFilter}, as every request is by the time a controller sees it.
     */
    @BeforeEach
    void setup() {
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setSecure(true);
        request.setServerName("catalogue.ceh.ac.uk");
        request.setServerPort(443);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assembler = new ServiceAgreementModelAssembler(documentRepository, serviceAgreementQualityService);
    }

    @AfterEach
    void clearRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * Both halves matter: the scheme the browser used, and no {@code :443} - an explicit default port
     * is what made the published URLs unusable in the first place.
     */
    private void assertPublicUrl(String href) {
        assertThat(href, startsWith("https://catalogue.ceh.ac.uk/"));
    }

    /**
     * The scheme used to be chosen from the active Spring profiles, so anything not named
     * "development" got {@code https} whether or not the request used it. Reading it from the request
     * is the change; a plain-HTTP request on a non-default port is where the two differ, and it is the
     * shape local development actually has.
     */
    @Test
    void linksFollowTheRequestRatherThanTheActiveProfiles() {
        //given a plain-HTTP request, as a developer running the app locally makes
        val request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(id);
        given(serviceAgreementQualityService.check(id)).willReturn(new Results(new ArrayList<>(), id));

        //when
        val model = assembler.toModel(serviceAgreement);

        //then
        assertThat(model.getLink("history").orElseThrow().getHref(), startsWith("http://localhost:8080/"));
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
        String relation = "publish";
        assertTrue(model.getLink(relation).isPresent());
        assertPublicUrl(model.getLink(relation).get().getHref());
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
        String relation = "history";
        assertTrue(model.getLink(relation).isPresent());
        assertPublicUrl(model.getLink(relation).get().getHref());
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
        String relation = "add-editor";
        assertTrue(model.getLink(relation).isPresent());
        assertPublicUrl(model.getLink(relation).get().getHref());
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
        String relation = "submit";
        assertTrue(model.getLink(relation).isPresent());
        assertPublicUrl(model.getLink(relation).get().getHref());
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
