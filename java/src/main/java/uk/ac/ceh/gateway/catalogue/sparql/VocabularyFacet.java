package uk.ac.ceh.gateway.catalogue.sparql;

import lombok.Getter;

@Getter
public enum VocabularyFacet {
    ASSIST_RESEARCH_THEMES("assist-research-themes"),
    ASSIST_TOPICS("assist-topics"),
    IMP_DATE_TYPE("dt"),
    TOPIC("topic"),
    WATER_POLLUTANT("wp"),
    INMS_DEMONSTRATION_REGION("region"),
    INMS_PROJECT("project"),
    MODEL_TYPE("Model_Type"),
    UKCEH_RESEARCH_THEME("research-theme"),
    UKCEH_RESEARCH_PROJECT("research-project"),
    UKCEH_SCIENCE_CHALLENGE("science-challenge"),
    UKCEH_SERVICE("service");

    private final String facetName;

    VocabularyFacet(String facetName) {
        this.facetName = facetName;
    }

}

