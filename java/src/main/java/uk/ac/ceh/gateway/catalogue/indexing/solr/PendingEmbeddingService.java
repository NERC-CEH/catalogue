package uk.ac.ceh.gateway.catalogue.indexing.solr;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collects document IDs that need vector embeddings and flushes them to Solr on a schedule.
 *
 * Decoupling embedding from the main indexing path means multiple rapid saves of the same
 * document result in only one Bedrock call (the pending map entry is overwritten each time).
 * Documents without embeddings remain fully searchable via BM25.
 */
@Slf4j
@Service
@ConditionalOnBean(EmbeddingModel.class)
public class PendingEmbeddingService {

    private final ConcurrentHashMap<String, SolrIndex> pending = new ConcurrentHashMap<>();

    private static final int MAX_EMBED_CHARS = 30_000;

    private final EmbeddingModel embeddingModel;
    private final SolrClient solrClient;
    private final Optional<SupportingDocumentExtractor> documentExtractor;
    private final int batchSize;
    private final long interBatchPauseMs;

    public PendingEmbeddingService(
            EmbeddingModel embeddingModel,
            SolrClient solrClient,
            Optional<SupportingDocumentExtractor> documentExtractor,
            @Value("${catalogue.embedding.batch-size:50}") int batchSize,
            @Value("${catalogue.embedding.inter-batch-pause-ms:1000}") long interBatchPauseMs
    ) {
        this.embeddingModel = embeddingModel;
        this.solrClient = solrClient;
        this.documentExtractor = documentExtractor;
        this.batchSize = batchSize;
        this.interBatchPauseMs = interBatchPauseMs;
        log.info("Creating — batch-size={}, inter-batch-pause={}ms, doc-extraction={}",
                batchSize, interBatchPauseMs, documentExtractor.isPresent());
    }

    /**
     * Mark a document as needing an embedding update. If the document was already pending,
     * the SolrIndex is replaced with the latest version so we always embed the most recent state.
     */
    public void mark(String rawId, SolrIndex index) {
        pending.put(rawId, index);
    }

    @Scheduled(fixedDelayString = "${catalogue.embedding.flush-delay:PT5M}")
    public void flush() {
        if (pending.isEmpty()) {
            return;
        }

        // Drain current entries; new marks that arrive during this flush are picked up next cycle
        Set<String> keys = new HashSet<>(pending.keySet());
        Map<String, SolrIndex> batch = new HashMap<>();
        for (String key : keys) {
            SolrIndex idx = pending.remove(key);
            if (idx != null) {
                batch.put(key, idx);
            }
        }

        if (batch.isEmpty()) {
            return;
        }

        log.info("Flushing embeddings for {} documents", batch.size());

        List<Map.Entry<String, SolrIndex>> entries = new ArrayList<>(batch.entrySet());
        List<List<Map.Entry<String, SolrIndex>>> chunks = Lists.partition(entries, batchSize);

        for (List<Map.Entry<String, SolrIndex>> chunk : chunks) {
            processChunk(chunk);
            if (chunks.size() > 1 && interBatchPauseMs > 0) {
                try {
                    Thread.sleep(interBatchPauseMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Embedding flush interrupted");
                    // Re-queue remaining entries
                    batch.forEach(pending::putIfAbsent);
                    return;
                }
            }
        }

        try {
            solrClient.commit("documents");
            log.info("Embedding flush complete for {} documents", batch.size());
        } catch (Exception e) {
            log.warn("Solr commit failed after embedding flush", e);
        }
    }

    private void processChunk(List<Map.Entry<String, SolrIndex>> chunk) {
        for (Map.Entry<String, SolrIndex> entry : chunk) {
            String rawId = entry.getKey();
            SolrIndex idx = entry.getValue();
            try {
                String metadataText = buildEmbeddingText(idx);
                String docText = documentExtractor.map(ext -> ext.extractText(rawId)).orElse("");
                String text = docText.isBlank() ? metadataText : (metadataText + " " + docText).trim();
                if (text.length() > MAX_EMBED_CHARS) {
                    text = text.substring(0, MAX_EMBED_CHARS);
                }
                if (text.isBlank()) {
                    log.debug("Skipping embedding for {} — no embeddable text", rawId);
                    continue;
                }
                float[] vec = embeddingModel.embed(text);

                SolrInputDocument update = new SolrInputDocument();
                update.addField("identifier", idx.getIdentifier());
                update.addField("vector", Map.of("set", toFloatList(vec)));
                update.addField("embedding_text", Map.of("set", text));
                // document_text is stored=false with no copyField, so it must be re-set
                // on every atomic update or the BM25 keyword index loses supporting doc text
                if (!docText.isBlank()) {
                    update.addField("document_text", Map.of("set", docText));
                }
                solrClient.add("documents", update);

                log.debug("Embedded document {}", rawId);
            } catch (Exception e) {
                log.warn("Embedding failed for {}, re-queuing for next flush", rawId, e);
                pending.putIfAbsent(rawId, idx);
            }
        }
    }

    String buildEmbeddingText(SolrIndex idx) {
        return Stream.of(
                idx.getTitle(),
                idx.getDescription(),
                joinList(idx.getKeyword()),
                joinList(idx.getAltTitle()),
                idx.getObjectives(),
                joinList(idx.getObservedPropertyTitle()),
                joinList(idx.getKeywordsParameters()),
                joinList(idx.getSupplementalDescription()),
                idx.getTemporalExtentText()
        )
        .filter(s -> s != null && !s.isBlank())
        .collect(Collectors.joining(" "));
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return String.join(" ", values);
    }

    private List<Float> toFloatList(float[] vec) {
        List<Float> list = new ArrayList<>(vec.length);
        for (float f : vec) {
            list.add(f);
        }
        return list;
    }
}
