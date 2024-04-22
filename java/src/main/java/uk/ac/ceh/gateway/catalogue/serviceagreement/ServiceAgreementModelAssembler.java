package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Profile("service-agreement")
@Slf4j
@Service
public class ServiceAgreementModelAssembler extends RepresentationModelAssemblerSupport<ServiceAgreement, ServiceAgreementModel> {
    private final DocumentRepository documentRepository;
    private final ServiceAgreementQualityService serviceAgreementQualityService;

    private final String activeProfiles;

    private String schema() {
        return activeProfiles.contains("development") ? "http" : "https";
    }

    public ServiceAgreementModelAssembler(DocumentRepository documentRepository,
            ServiceAgreementQualityService serviceAgreementQualityService,
            Environment env
) {
        super(ServiceAgreementController.class, ServiceAgreementModel.class);
        this.documentRepository = documentRepository;
        this.serviceAgreementQualityService = serviceAgreementQualityService;
        this.activeProfiles = String.join(",", env.getActiveProfiles());
    }

    @Override
    public ServiceAgreementModel toModel(ServiceAgreement serviceAgreement) {
        return createModelWithId(
                serviceAgreement.getId(),
                serviceAgreement
                );
    }

    @SneakyThrows
    @Override
    protected ServiceAgreementModel instantiateModel(ServiceAgreement serviceAgreement) {
        val model = new ServiceAgreementModel(serviceAgreement);
        val id = serviceAgreement.getId();

        val historyLink = Link.of(
                linkTo(methodOn(ServiceAgreementController.class).getHistory(id)).toUriComponentsBuilder().scheme(this.schema()).build().toUriString(),
                "history"
            ).withTitle("History");
        model.add(historyLink);

        if ("draft".equals(serviceAgreement.getState()) &&
                serviceAgreementQualityService.check(serviceAgreement.getId()).getErrors() == 0) {
            val submitLink = Link.of(
                linkTo(methodOn(ServiceAgreementController.class).submitServiceAgreement(null, id)).toUriComponentsBuilder().scheme(this.schema()).build().toUriString(),
                "submit"
            ).withTitle("Submit");
            model.add(submitLink);
        }

        if ("pending publication".equals(serviceAgreement.getState())) {
            val gemini = documentRepository.read(serviceAgreement.getId());
            if (gemini.getState().equals("draft")) {
                val publishLink = Link.of(
                    linkTo(methodOn(ServiceAgreementController.class).publishServiceAgreement(null, id)).toUriComponentsBuilder().scheme(this.schema()).build().toUriString(),
                    "publish"
                ).withTitle("Publish");
                model.add(publishLink);
                val addEdit = Link.of(
                    linkTo(methodOn(ServiceAgreementController.class).giveDepositorEditPermission(null, id)).toUriComponentsBuilder().scheme(this.schema()).build().toUriString(),
                    "add-editor"
                ).withTitle("Further Edits Required");
                model.add(addEdit);
            }
        }
        log.debug("model: {}", model);
        return model;
    }
}
