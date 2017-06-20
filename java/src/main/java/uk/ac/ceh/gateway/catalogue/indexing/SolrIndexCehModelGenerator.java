package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;

public class SolrIndexCehModelGenerator implements IndexGenerator<MetadataDocument, SolrIndex>{
    public static final String INMS_SCALE_URL = "http://vocabs.ceh.ac.uk/inms/scale/";
    public static final String INMS_TOPIC_URL = "http://vocabs.ceh.ac.uk/inms/topic/";
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;

    @Autowired
    public SolrIndexCehModelGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex) {
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
    }

    @Override
    public SolrIndex generateIndex(MetadataDocument document) throws DocumentIndexingException {
        return metadataDocumentSolrIndex
            .generateIndex(document)
                .setImpScale(impScale(document))
                .setImpTopic(grab(getKeywordsFilteredByUrlFragment(document, INMS_TOPIC_URL), Keyword::getValue));
    }
    
    private List<Keyword> getKeywordsFilteredByUrlFragment(MetadataDocument document, String urlFragment) {
        return Optional.ofNullable(document.getAllKeywords())
                .orElse(Collections.emptyList())
                .stream()
                .filter(k -> k.getUri().startsWith(urlFragment))
                .collect(Collectors.toList());
    }
    
    private List<String> impScale(MetadataDocument document) {
        List<String> toReturn = grab(getKeywordsFilteredByUrlFragment(document, INMS_SCALE_URL), Keyword::getValue);
        if (document instanceof CehModelApplication) {
            CehModelApplication application = (CehModelApplication) document;
            Optional.ofNullable(application.getModelInfos())
                .orElse(Collections.emptyList())
                .stream()
                .filter(mi -> mi.getSpatialExtentOfApplication() != null && !mi.getSpatialExtentOfApplication().isEmpty())
                .forEach(mi -> toReturn.add(mi.getSpatialExtentOfApplication()));
        }
        return toReturn;
    }

}