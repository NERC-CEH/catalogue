package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Indexing monitoring programme documents into Solr")
@ExtendWith(MockitoExtension.class)
class SolrIndexMonitoringProgrammeGeneratorTest {
    @Mock
    SolrIndexMetadataDocumentGenerator documentIndexer;
    private SolrIndexMonitoringProgrammeGenerator generator;

    @BeforeEach
    void init() {
        when(documentIndexer.generateIndex(any(MetadataDocument.class))).thenReturn(new SolrIndex());
        generator = new SolrIndexMonitoringProgrammeGenerator(documentIndexer);
    }

    @Test
    public void checkThatMonitoringProgrammeDataTransferedToIndex() {
        //Given

        MonitoringProgramme document = new MonitoringProgramme();
        document.setObjectives(SolrIndexMonitoringGeneratorData.objectives);
        document.setEnvironmentalDomain(SolrIndexMonitoringGeneratorData.domainList);
        document.setKeywordsParameters(SolrIndexMonitoringGeneratorData.paramList);
        document.setResponsibleParties(SolrIndexMonitoringGeneratorData.orgList);
        document.setOperatingPeriod(SolrIndexMonitoringGeneratorData.periodList);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals(SolrIndexMonitoringGeneratorData.expectedObjectives, index.getObjectives());
        assertThat(SolrIndexMonitoringGeneratorData.expectedDomainList, equalTo(index.getEnvironmentalDomains()));
        assertThat(SolrIndexMonitoringGeneratorData.expectedParamList, equalTo(index.getKeywordsParameters()));
        assertThat(SolrIndexMonitoringGeneratorData.expectedOrgList, equalTo(index.getResponsibleParties()));
        assertThat(SolrIndexMonitoringGeneratorData.expectedPeriodList, equalTo(index.getOperatingPeriod()));

    }
}
