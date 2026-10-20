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
 * repeats it on the real paths and returns the file's real path.
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
            // The checks above are lexical, and the file system follows links, so a symbolic link on
            // the share, for the record directory or the file, could still lead anywhere the
            // container can read. Repeat them on the real paths.
            Path realBase = normalisedBase.toRealPath();
            Path realRecord = recordDirectory.toRealPath();
            if (!realRecord.startsWith(realBase) || realRecord.equals(realBase)) {
                return Optional.empty();
            }
            Path real = file.toRealPath();
            if (!real.startsWith(realRecord) || !Files.isRegularFile(real) || !Files.isReadable(real)) {
                return Optional.empty();
            }
            // The real path is returned so the caller opens exactly what was checked; its name may
            // differ from the listed one, so callers decide how to show a file by the listed name.
            return Optional.of(real);
        } catch (InvalidPathException | IOException e) {
            return Optional.empty();
        }
    }
}
