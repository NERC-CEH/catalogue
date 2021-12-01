package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SolrScheduledReindexServiceTest {
    @Mock
    private DocumentIndexingService documentIndexingService;

    @Test
    @SneakyThrows
    public void reindex() {
        //given
        val service = new SolrScheduledReindexService(documentIndexingService);

        //when
        service.reindex();

        //then
        verify(documentIndexingService).attemptIndexing();
    }
}
