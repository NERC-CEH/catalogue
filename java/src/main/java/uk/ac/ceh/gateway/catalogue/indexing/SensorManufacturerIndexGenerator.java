package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.elter.SensorDocument;


@AllArgsConstructor
public class SensorManufacturerIndexGenerator implements IndexGenerator<SensorDocument, SolrIndex> {

    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    
    @Override
    public SolrIndex generateIndex(SensorDocument document) {
        return metadataDocumentSolrIndex
            .generateIndex(document)
            .setManufacturer(document.getManufacturer())
            .setManufacturerName(document.getManufacturerName());
    }
}
