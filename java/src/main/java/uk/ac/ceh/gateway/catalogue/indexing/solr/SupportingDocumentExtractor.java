package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extracts plain text from PDF, Word, and RTF supporting documents for a given catalogue record.
 * Only active when {@code catalogue.supporting-documents.location} is configured.
 */
@Slf4j
@Service
@ConditionalOnProperty("catalogue.supporting-documents.location")
public class SupportingDocumentExtractor {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".pdf", ".doc", ".docx", ".rtf");

    private final Path basePath;
    private final int maxCharsPerFile;
    private final int maxFiles;

    public SupportingDocumentExtractor(
            @Value("${catalogue.supporting-documents.location}") String location,
            @Value("${catalogue.embedding.doc-max-chars:4000}") int maxCharsPerFile,
            @Value("${catalogue.embedding.doc-max-files:5}") int maxFiles
    ) {
        this.basePath = Path.of(location);
        this.maxCharsPerFile = maxCharsPerFile;
        this.maxFiles = maxFiles;
    }

    /**
     * Extracts text from supporting documents for the given document ID.
     * Returns empty string when the directory is absent, contains no extractable files,
     * or extraction fails — caller treats this as "no supporting documents available".
     */
    public String extractText(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            return "";
        }
        Path docDir = basePath.resolve(documentId).normalize();
        if (!docDir.startsWith(basePath.normalize())) {
            log.warn("Rejected document ID '{}' — resolved path escapes base directory", documentId);
            return "";
        }
        if (!Files.isDirectory(docDir)) {
            return "";
        }
        try (Stream<Path> files = Files.walk(docDir, 1)) {
            return files
                    .filter(this::isSupportedFile)
                    .limit(maxFiles)
                    .map(this::extractFromFile)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(" "));
        } catch (IOException e) {
            log.warn("Failed to list supporting documents for {}", documentId, e);
            return "";
        }
    }

    private String extractFromFile(Path file) {
        try {
            TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(file));
            String text = reader.get().stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.joining(" "));
            return text.length() > maxCharsPerFile ? text.substring(0, maxCharsPerFile) : text;
        } catch (Exception e) {
            log.warn("Text extraction failed for {}", file, e);
            return "";
        }
    }

    private boolean isSupportedFile(Path p) {
        if (!Files.isRegularFile(p)) return false;
        String name = p.getFileName().toString().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
