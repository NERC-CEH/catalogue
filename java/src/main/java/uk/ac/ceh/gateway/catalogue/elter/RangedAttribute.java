package uk.ac.ceh.gateway.catalogue.elter;

import lombok.Data;

@Data
public class RangedAttribute {
    private String comment;
    private String minimumValue;
    private String maximumValue;
    private String textUnit;
    private String vocabUnit;
}