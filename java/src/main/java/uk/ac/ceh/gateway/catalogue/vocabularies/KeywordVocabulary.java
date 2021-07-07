package uk.ac.ceh.gateway.catalogue.vocabularies;

import org.springframework.scheduling.annotation.Scheduled;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;

import javax.naming.AuthenticationException;
import java.util.List;

public interface KeywordVocabulary {
    final int ONE_MINUTE = 60000;
    final int SEVEN_DAYS = 604800000;

    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    public void retrieveVocabularies() throws DocumentIndexingException, AuthenticationException;

    public String getName();

    public List<String> getCatalogues();
}
