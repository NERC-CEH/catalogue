package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Maps a record id and a Hubbub-relative path onto a file under a share, refusing anything that
 * would escape {@code <base>/<recordId>/}. Uses the same containment idiom as
 * {@code SupportingDocumentExtractor.extractText}.
 */
@Component
public class PreviewFileResolver {

    public Optional<Path> resolve(Path base, String recordId, String hubbubPath) {
        if (base == null || recordId == null || recordId.isBlank()
            || hubbubPath == null || hubbubPath.isBlank()) {
            return Optional.empty();
        }
        try {
            Path normalisedBase = base.normalize();
            Path recordDirectory = normalisedBase.resolve(recordId).normalize();
            if (!recordDirectory.startsWith(normalisedBase) || recordDirectory.equals(normalisedBase)) {
                return Optional.empty();
            }
            Path file = recordDirectory.resolve(hubbubPath).normalize();
            if (!file.startsWith(recordDirectory)) {
                return Optional.empty();
            }
            return Files.isRegularFile(file) && Files.isReadable(file)
                ? Optional.of(file)
                : Optional.empty();
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }
}
