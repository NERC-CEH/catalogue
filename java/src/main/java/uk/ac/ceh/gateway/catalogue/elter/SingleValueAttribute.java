package uk.ac.ceh.gateway.catalogue.elter;

import lombok.Data;

@Data
public class SingleValueAttribute {
    private String comment;
    private String value;
    private String textUnit;
    private String vocabUnit;
}