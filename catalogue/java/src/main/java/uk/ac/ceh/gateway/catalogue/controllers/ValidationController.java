package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.indexing.ValidationIndexingService;
import uk.ac.ceh.gateway.catalogue.model.ValidationResponse;
import uk.ac.ceh.gateway.catalogue.validation.ValidationLevel;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;
import uk.ac.ceh.gateway.catalogue.validation.ValidatorResult;
import uk.ac.ceh.gateway.catalogue.validation.ValidatorResult.ValidatorState;

/**
 *
 * @author cjohn
 */
@Controller
@Slf4j
@RequestMapping("maintenance/validation")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class ValidationController {
    private final ValidationIndexingService<?> validationIndexingService;
    
    @Autowired
    public ValidationController(ValidationIndexingService validationIndexingService) {
        this.validationIndexingService = validationIndexingService;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ValidationResponse getValidationResults() {
        Map<String,Map<ValidationLevel,ValidatorState>> toReturn = new HashMap<>();
        
        for(ValidationReport report: validationIndexingService.getResults() ) {
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
        return new ValidationResponse(results, validationIndexingService.getFailed());
    }
}
