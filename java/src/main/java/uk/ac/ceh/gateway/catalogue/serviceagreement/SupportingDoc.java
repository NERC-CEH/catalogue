package uk.ac.ceh.gateway.catalogue.serviceagreement;

import java.util.List;
import lombok.Data;

@Data
public class SupportingDoc {
    private String name;
    private String format;
    private List<String> content;

    // Remember to update the corresponding strings in the JavaScript
    // code in web/src/editor/src/models/service-agreement/SupportingDoc.js
    // if you change any of these

    public String contentTypeToText(String type) {
        switch (type) {
        case "generationMethods":
            return "Collection/generation methods";
        case "natureUnits":
            return "Nature and Units of recorded values";
        case "qc":
            return "Quality control";
        case "dataStructure":
            return "Details of data structure";
        case "experimentalDesign":
            return "Experimental design/Sampling regime";
        case "instrumentation":
            return "Fieldwork and laboratory instrumentation";
        case "calibrationSteps":
            return "Calibration steps and values";
        case "analyticalMethods":
            return "Analytical methods";
        case "other":
            return "Any other information useful to the interpretation of the data";
        default:
            throw new IllegalArgumentException("Unknown content type: " + type);
        }
    }
}
