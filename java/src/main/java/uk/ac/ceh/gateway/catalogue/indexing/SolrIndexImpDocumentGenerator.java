package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_BROADER_CATCHMENT_ISSUES_URL;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_SCALE_URL;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_WATER_QUALITY_URL;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;

public class SolrIndexImpDocumentGenerator implements IndexGenerator<ImpDocument, SolrIndex>{
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;

    @Autowired
    public SolrIndexImpDocumentGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex) {
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
    }

    @Override
    public SolrIndex generateIndex(ImpDocument document) throws DocumentIndexingException {
        return metadataDocumentSolrIndex
            .generateIndex(document)
                .setImpBroaderCatchmentIssues(grab(getKeywordsFilteredByUrlFragment(document, IMP_BROADER_CATCHMENT_ISSUES_URL), Keyword::getValue))
                .setImpScale(grab(getKeywordsFilteredByUrlFragment(document, IMP_SCALE_URL), Keyword::getValue))
                .setImpWaterQuality(grab(getKeywordsFilteredByUrlFragment(document, IMP_WATER_QUALITY_URL), Keyword::getValue));
    }
    
    private List<Keyword> getKeywordsFilteredByUrlFragment(ImpDocument document, String urlFragment) {
        return Optional.ofNullable(document.getKeywords())
                .orElse(Collections.emptyList())
                .stream()
                .filter(k -> {
                    return k.getUri().startsWith(urlFragment);
                })
                .collect(Collectors.toList());
    }

}