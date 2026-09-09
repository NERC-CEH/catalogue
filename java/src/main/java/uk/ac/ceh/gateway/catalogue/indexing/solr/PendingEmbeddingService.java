package uk.ac.ceh.gateway.catalogue.indexing.solr;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.RemoteSolrException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
// Gated on the profile rather than @ConditionalOnBean(EmbeddingModel.class): that annotation is
// only reliable on auto-configuration classes. On a component-scanned @Service it is evaluated
// while scanning, before Spring AI registers the embedding model, so it never matched and this
// bean never loaded — silently, because every consumer injects it as an Optional.
@Profile("vector-search")
public class PendingEmbeddingService {

    private final ConcurrentHashMap<String, SolrIndex> pending = new ConcurrentHashMap<>();

    /**
     * Consecutive failures per document. Without this a document that can never be embedded comes
     * back on every flush for the lifetime of the process, spending an embedding call and logging a
     * warning each time, and never escalating to anything anyone would notice.
     */
    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();

    /**
     * Documents abandoned after {@link #maxAttempts}, and why. Retained so the failure outlives the
     * log line that reported it — see {@link #abandonedEmbeddings()}.
     */
    private final ConcurrentHashMap<String, String> abandoned = new ConcurrentHashMap<>();

    private final AtomicInteger consecutiveCommitFailures = new AtomicInteger();

    private static final int MAX_EMBED_CHARS = 30_000;

    private final EmbeddingModel embeddingModel;
    private final SolrClient solrClient;
    private final Optional<SupportingDocumentExtractor> documentExtractor;
    private final int batchSize;
    private final long interBatchPauseMs;
    private final int maxAttempts;

    public PendingEmbeddingService(
            EmbeddingModel embeddingModel,
            SolrClient solrClient,
            Optional<SupportingDocumentExtractor> documentExtractor,
            @Value("${catalogue.embedding.batch-size:50}") int batchSize,
            @Value("${catalogue.embedding.inter-batch-pause-ms:1000}") long interBatchPauseMs,
            @Value("${catalogue.embedding.max-attempts:5}") int maxAttempts
    ) {
        this.embeddingModel = embeddingModel;
        this.solrClient = solrClient;
        this.documentExtractor = documentExtractor;
        this.batchSize = batchSize;
        this.interBatchPauseMs = interBatchPauseMs;
        this.maxAttempts = maxAttempts;
        log.info("Creating — batch-size={}, inter-batch-pause={}ms, max-attempts={}, doc-extraction={}",
                batchSize, interBatchPauseMs, maxAttempts, documentExtractor.isPresent());
    }

    /**
     * Documents given up on, keyed by raw id, with the reason. Empty in normal operation; entries
     * mean those documents have no vector and will not get one until they are saved again or the
     * index is rebuilt. They stay keyword-searchable via BM25 either way.
     */
    public Map<String, String> abandonedEmbeddings() {
        return Map.copyOf(abandoned);
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

        List<String> added = new ArrayList<>();
        for (List<Map.Entry<String, SolrIndex>> chunk : chunks) {
            added.addAll(processChunk(chunk));
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

        if (added.isEmpty()) {
            // Nothing reached Solr, so there is nothing to commit and no point provoking a second
            // failure. Whatever failed has already been re-queued or abandoned by processChunk.
            return;
        }

        try {
            solrClient.commit("documents");
            consecutiveCommitFailures.set(0);
            log.info("Embedding flush complete for {} documents", added.size());
        } catch (Exception e) {
            // The updates reached Solr but are not searchable until a commit succeeds, and this
            // flush has already removed them from the pending map. Put them back so the next flush
            // re-applies them; previously they were dropped here behind a single warning.
            added.forEach(rawId -> {
                SolrIndex idx = batch.get(rawId);
                if (idx != null) {
                    pending.putIfAbsent(rawId, idx);
                }
            });
            int failures = consecutiveCommitFailures.incrementAndGet();
            if (failures >= maxAttempts) {
                log.error("Solr commit has failed {} flushes running — {} embeddings are queued and "
                        + "none are searchable. Check Solr; the work is retained, not lost.",
                        failures, pending.size(), e);
            } else {
                log.warn("Solr commit failed after embedding flush, re-queued {} documents (failure {} of {})",
                        added.size(), failures, maxAttempts, e);
            }
        }
    }

    /**
     * @return the raw ids successfully handed to Solr, so a failed commit can put them back.
     */
    private List<String> processChunk(List<Map.Entry<String, SolrIndex>> chunk) {
        List<String> added = new ArrayList<>();
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

                attempts.remove(rawId);
                abandoned.remove(rawId);
                added.add(rawId);
                log.debug("Embedded document {}", rawId);
            } catch (Exception e) {
                recordFailure(rawId, idx, e);
            }
        }
        return added;
    }

    /**
     * Decides whether a failure is worth retrying. The distinction matters because the retry budget
     * exists to stop a permanently broken document cycling forever — spending it on an outage
     * instead would turn a temporary problem into permanent data loss.
     */
    private void recordFailure(String rawId, SolrIndex idx, Exception e) {
        if (isRejectedBySolr(e)) {
            abandon(rawId, e, "Solr rejected the document");
            return;
        }
        if (isBackendUnreachable(e)) {
            log.warn("Embedding for {} deferred, backend unreachable: {}", rawId, e.toString());
            pending.putIfAbsent(rawId, idx);
            return;
        }
        int count = attempts.merge(rawId, 1, Integer::sum);
        if (count >= maxAttempts) {
            abandon(rawId, e, "failed " + count + " consecutive attempts");
        } else {
            log.warn("Embedding failed for {} (attempt {} of {}), re-queuing for next flush",
                    rawId, count, maxAttempts, e);
            // putIfAbsent, not put: a save during this flush may already have queued a newer index,
            // which must not be overwritten with the stale one being retried.
            pending.putIfAbsent(rawId, idx);
        }
    }

    private void abandon(String rawId, Exception e, String reason) {
        attempts.remove(rawId);
        abandoned.put(rawId, reason + ": " + e);
        log.error("Giving up on the embedding for {} — {}. It stays keyword-searchable but has no "
                + "vector; save it again or rebuild the index to retry.", rawId, reason, e);
    }

    /**
     * A 4xx from Solr will happen identically every time — a schema or field-value problem with this
     * document — so there is nothing to retry. {@code RemoteSolrException} is a sibling of
     * {@code SolrServerException} rather than a subclass, so it has to be matched in its own right.
     */
    private static boolean isRejectedBySolr(Throwable e) {
        return causeOfType(e, RemoteSolrException.class)
                .filter(remote -> remote.code() >= 400 && remote.code() < 500)
                .isPresent();
    }

    /**
     * A backend that cannot be reached says nothing about the document, so it must not consume the
     * document's attempts.
     */
    private static boolean isBackendUnreachable(Throwable e) {
        return causeOfType(e, IOException.class).isPresent()
                || causeOfType(e, SolrServerException.class).isPresent();
    }

    private static <T extends Throwable> Optional<T> causeOfType(Throwable e, Class<T> type) {
        for (Throwable t = e; t != null && t != t.getCause(); t = t.getCause()) {
            if (type.isInstance(t)) {
                return Optional.of(type.cast(t));
            }
        }
        return Optional.empty();
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
