package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Profile("service-agreement")
@Slf4j
@Service
public class ServiceAgreementModelAssembler extends RepresentationModelAssemblerSupport<ServiceAgreement, ServiceAgreementModel> {
    private final DocumentRepository documentRepository;

    public ServiceAgreementModelAssembler(DocumentRepository documentRepository) {
        super(ServiceAgreementController.class, ServiceAgreementModel.class);
        this.documentRepository = documentRepository;
        log.info("Creating");
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
        if ("draft".equals(serviceAgreement.getState())) {
            val link = linkTo(methodOn(ServiceAgreementController.class)
                    .submitServiceAgreement(
                            null,
                            serviceAgreement.getId()
                    ))
                    .withRel("submit")
                    .withTitle("Submit");
            model.add(link);
        }
        if ("pending publication".equals(serviceAgreement.getState())) {
            val link = linkTo(methodOn(ServiceAgreementController.class)
                    .publishServiceAgreement(
                            null,
                            serviceAgreement.getId()
                    ))
                    .withRel("publish")
                    .withTitle("Publish");
            model.add(link);
        }
        if ("published".equals(serviceAgreement.getState())) {
            val gemini = documentRepository.read(serviceAgreement.getId());
            if (gemini.getState().equals("draft")) {
                val link = linkTo(methodOn(ServiceAgreementController.class)
                        .populateGeminiDocument(
                                null,
                                serviceAgreement.getId()
                        ))
                        .withRel("populate")
                        .withTitle("Populate Metadata");
                model.add(link);
            }
        }
        log.debug("model: {}", model);
        return model;
    }
}
