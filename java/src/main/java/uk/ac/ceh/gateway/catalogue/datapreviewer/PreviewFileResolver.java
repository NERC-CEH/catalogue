package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Maps a record id and a Hubbub-relative path onto a file under a share, refusing anything that
 * would escape {@code <base>/<recordId>/}, whether by the path itself or through a symbolic link.
 * Uses the same lexical containment idiom as {@code SupportingDocumentExtractor.extractText}, then
 * checks the real path.
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
            if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
                return Optional.empty();
            }
            // The checks above are lexical, and isRegularFile follows links, so a symbolic link on
            // the share could still lead anywhere the container can read. Compare real paths too. The
            // listed path, not the real one, is returned, as its extension decides how it is shown.
            return file.toRealPath().startsWith(recordDirectory.toRealPath())
                ? Optional.of(file)
                : Optional.empty();
        } catch (InvalidPathException | IOException e) {
            return Optional.empty();
        }
    }
}
