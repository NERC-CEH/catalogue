package uk.ac.ceh.gateway.catalogue.indexing.solr;

import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.Arrays;
import java.util.List;

public class SolrIndexMonitoringGeneratorData {
    static String objectives = "objective";
    static String expectedObjectives = "objective";

    static Keyword facilityType = Keyword.builder().value("facility_type").build();
    static String expectedFacilityType = "facility_type";

    static List<Keyword> domainList = Arrays.asList(
        Keyword.builder().value("domain_1").build(),
        Keyword.builder().value("domain_2").build()
    );
    static List<String> expectedDomainList = Arrays.asList(
        "domain_1",
        "domain_2"
    );

    static List<Keyword> paramList = Arrays.asList(
        Keyword.builder().value("param_1").build(),
        Keyword.builder().value("param_2").build()
    );
    static List<String> expectedParamList = Arrays.asList(
        "param_1",
        "param_2"
    );

    static List<ResponsibleParty> orgList = Arrays.asList(
        ResponsibleParty.builder().organisationName("org_1").build(),
        ResponsibleParty.builder().organisationName("org_2").build()
    );
    static List<String> expectedOrgList = Arrays.asList(
        "org_1",
        "org_2"
    );

    static List<TimePeriod> periodList = Arrays.asList(
        TimePeriod.builder().begin("2024-01-01").end("2024-02-01").build(),
        TimePeriod.builder().end("2024-04-01").build()
    );
    static List<String> expectedPeriodList = Arrays.asList(
        "[2024-01-01 TO 2024-02-01]",
        "[* TO 2024-04-01]"
    );

}
