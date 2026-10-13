package uk.ac.ceh.gateway.catalogue.upload;

import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadException;

import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Confines upload paths built from client-supplied names to the record's own folder.
 * <p>
 * Record ids, upload filenames and zip entry names all come from the request, so joining them
 * onto a storage root is only safe once the result is normalised and checked. Without that, a
 * user with upload rights on one record could write or delete files anywhere the application
 * can (NERC-CEH/dri-one#453).
 */
public final class UploadPaths {

    private UploadPaths() {}

    /**
     * Resolves {@code relative} inside {@code root/id}, the way {@code Path.of(root, id, relative)}
     * joins them (an absolute {@code relative} is joined as a relative one), and refuses any
     * result that leaves the record's folder.
     *
     * @return the normalised absolute path, always strictly inside {@code root/id}
     * @throws UploadException if the id leaves {@code root} or the path leaves {@code root/id}
     */
    public static Path within(String root, String id, String relative) {
        Path base = Path.of(root).toAbsolutePath().normalize();
        Path folder = Path.of(root, id).toAbsolutePath().normalize();
        if (folder.equals(base) || !folder.startsWith(base)) {
            throw new UploadException(format("Invalid record id %s for upload", id));
        }
        Path target = Path.of(root, id, relative).toAbsolutePath().normalize();
        if (target.equals(folder) || !target.startsWith(folder)) {
            throw new UploadException(format("%s is outside the upload folder for %s", relative, id));
        }
        return target;
    }

    /**
     * The record's upload folder for a path returned by {@link #within}, so callers can report
     * a path relative to it.
     */
    public static Path folder(String root, String id) {
        return Path.of(root, id).toAbsolutePath().normalize();
    }
}
