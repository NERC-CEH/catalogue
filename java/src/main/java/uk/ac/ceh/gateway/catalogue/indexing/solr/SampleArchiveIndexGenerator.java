package uk.ac.ceh.gateway.catalogue.indexing.solr;

import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;

import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

public class SampleArchiveIndexGenerator implements IndexGenerator<SampleArchive, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;

    public SampleArchiveIndexGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentGenerator) {
        this.metadataDocumentGenerator = metadataDocumentGenerator;
    }

    @Override
    public SolrIndex generateIndex(SampleArchive document) throws DocumentIndexingException {
        return metadataDocumentGenerator.generateIndex(document)
            .setSaPhysicalState(grab(document.getPhysicalStates(), Keyword::getValue))
            .setSaSpecimenType(grab(document.getSpecimenTypes(), Keyword::getValue))
            .setSaTaxon(grab(document.getTaxa(), Keyword::getValue))
            .setSaTissue(grab(document.getTissues(), Keyword::getValue))
            ;
    }
}
