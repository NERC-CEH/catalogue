package uk.ac.ceh.gateway.catalogue.indexing.solr;

import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.samples.Sample;

import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

public class SolrIndexSampleGenerator implements IndexGenerator<Sample, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;

    public SolrIndexSampleGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentGenerator) {
        this.metadataDocumentGenerator = metadataDocumentGenerator;
    }

    @Override
    public SolrIndex generateIndex(Sample document) throws DocumentIndexingException {
        return metadataDocumentGenerator.generateIndex(document)
            //.setFunder(grab(document.getFunding(), Funding::getFunderName))
            ;
    }
}
