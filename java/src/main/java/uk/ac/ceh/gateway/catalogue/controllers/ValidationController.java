package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ceh.gateway.catalogue.indexing.validation.ValidationIndexingService;
import uk.ac.ceh.gateway.catalogue.model.ValidationResponse;
import uk.ac.ceh.gateway.catalogue.validation.ValidationLevel;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.ValidatorResult;
import uk.ac.ceh.gateway.catalogue.validation.ValidatorResult.ValidatorState;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@ToString
@RequestMapping("maintenance/validation")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class ValidationController {
    private final ValidationIndexingService indexingService;

    public ValidationController(
        @Qualifier("validation-index") ValidationIndexingService indexingService
    ) {
        this.indexingService = indexingService;
        log.info("Creating");
    }

    @GetMapping
    public ValidationResponse getValidationResults() {
        Map<String,Map<ValidationLevel,ValidatorState>> toReturn = new HashMap<>();

        for(ValidationReport report: indexingService.getResults() ) {
            for(Map.Entry<String, ValidationLevel> documentState: report.getResults().entrySet()) {
                toReturn.putIfAbsent(documentState.getKey(), new EnumMap<>(ValidationLevel.class));
                ValidationLevel level = documentState.getValue();

                Map<ValidationLevel, ValidatorState> validationState = toReturn.get(documentState.getKey());
                validationState.putIfAbsent(level, new ValidatorState(level, new ArrayList<>()));
                validationState.get(level).getDocuments().add(report.getDocumentId());
            }
        }

        List<ValidatorResult> results = toReturn
                .entrySet()
                .stream()
                .map((e) -> new ValidatorResult(e.getKey(), new ArrayList<>(e.getValue().values())))
                .collect(Collectors.toList());
        return new ValidationResponse(results, indexingService.getFailed());
    }
}
