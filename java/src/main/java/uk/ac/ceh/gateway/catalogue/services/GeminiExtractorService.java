
package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

/**
 * The following service defines some methods which help in extracting details
 * from Gemini Documents and returning these as easy to use entities. Designed
 * to be freemarker friendly.
 * @author cjohn
 */
public class GeminiExtractorService {
    
    public List<String> getKeywords(List<DescriptiveKeywords> keywords) {
        return keywords.stream()
                .filter((dk) -> dk.getThesaurusName() == null)
                .map(DescriptiveKeywords::getKeywords)
                .flatMap((k) -> k.stream())
                .map(Keyword::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
}
