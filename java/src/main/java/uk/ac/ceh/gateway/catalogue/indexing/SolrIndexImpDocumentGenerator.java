package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_CAMMP_ISSUES_URL;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_DATA_TYPE_URL;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_SCALE_URL;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_TOPIC_URL;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexGeminiDocumentGenerator.IMP_WATER_POLLUTANT_URL;
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
                .setImpCaMMPIssues(grab(getKeywordsFilteredByUrlFragment(document, IMP_CAMMP_ISSUES_URL), Keyword::getValue))
                .setImpDataType(grab(getKeywordsFilteredByUrlFragment(document, IMP_DATA_TYPE_URL), Keyword::getValue))
                .setImpScale(impScale(document))
                .setImpTopic(grab(getKeywordsFilteredByUrlFragment(document, IMP_TOPIC_URL), Keyword::getValue))
                .setImpWaterPollutant(grab(getKeywordsFilteredByUrlFragment(document, IMP_WATER_POLLUTANT_URL), Keyword::getValue));
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
    
    private List<String> impScale(ImpDocument document) {
        List<String> toReturn = grab(getKeywordsFilteredByUrlFragment(document, IMP_SCALE_URL), Keyword::getValue);
        if (document instanceof Model) {
            String applicationScale = ((Model) document).getApplicationScale();
            toReturn.add(applicationScale);
        }
        return toReturn;
    }

}