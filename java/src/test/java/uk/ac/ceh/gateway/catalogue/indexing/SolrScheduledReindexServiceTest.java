package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
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
        given(documentIndexingService.isIndexEmpty()).willReturn(true);

        //when
        service.reindex();

        //then
        verify(documentIndexingService).isIndexEmpty();
        verify(documentIndexingService).rebuildIndex();
    }
}