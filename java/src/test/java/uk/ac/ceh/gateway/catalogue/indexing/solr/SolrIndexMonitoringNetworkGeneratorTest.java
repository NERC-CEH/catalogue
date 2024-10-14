package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Indexing monitoring network documents into Solr")
@ExtendWith(MockitoExtension.class)
class SolrIndexMonitoringNetworkGeneratorTest {
    @Mock
    SolrIndexMetadataDocumentGenerator documentIndexer;
    private SolrIndexMonitoringNetworkGenerator generator;

    @BeforeEach
    void init() {
        when(documentIndexer.generateIndex(any(MetadataDocument.class))).thenReturn(new SolrIndex());
        generator = new SolrIndexMonitoringNetworkGenerator(documentIndexer);
    }

    @Test
    public void checkThatMonitoringNetworkDataTransferedToIndex() {
        //Given
        MonitoringNetwork document = new MonitoringNetwork();
        document.setObjectives(SolrIndexMonitoringGeneratorData.objectives);
        document.setEnvironmentalDomain(SolrIndexMonitoringGeneratorData.domainList);
        document.setKeywordsParameters(SolrIndexMonitoringGeneratorData.paramList);
        document.setResponsibleParties(SolrIndexMonitoringGeneratorData.orgList);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals(SolrIndexMonitoringGeneratorData.expectedObjectives, index.getObjectives());
        assertThat(SolrIndexMonitoringGeneratorData.expectedDomainList, equalTo(index.getEnvironmentalDomains()));
        assertThat(SolrIndexMonitoringGeneratorData.expectedParamList, equalTo(index.getKeywordsParameters()));
        assertThat(SolrIndexMonitoringGeneratorData.expectedOrgList, equalTo(index.getResponsibleParties()));

    }
}
