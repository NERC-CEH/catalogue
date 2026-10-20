package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PreviewFileResolverTest {
    private static final String RECORD = "rec-1";

    @TempDir
    Path base;

    private final PreviewFileResolver resolver = new PreviewFileResolver();

    @BeforeEach
    void layOutShare() throws IOException {
        Files.createDirectories(base.resolve(RECORD).resolve("sub"));
        Files.createDirectories(base.resolve("other"));
        Files.writeString(base.resolve(RECORD).resolve("a.csv"), "a\n1\n");
        Files.writeString(base.resolve(RECORD).resolve("sub").resolve("b.csv"), "b\n2\n");
        Files.writeString(base.resolve(RECORD).resolve("fish_biometry .csv"), "c\n3\n");
        Files.writeString(base.resolve("other").resolve("c.csv"), "secret\n");
    }

    @Test
    void resolvesFileInRecordDirectory() throws IOException {
        assertThat(resolver.resolve(base, RECORD, "a.csv"),
            is(Optional.of(base.resolve(RECORD).resolve("a.csv").toRealPath())));
    }

    @Test
    void resolvesFileInSubdirectory() {
        assertThat(resolver.resolve(base, RECORD, "sub/b.csv").isPresent(), equalTo(true));
    }

    @Test
    void resolvesNameWithSpace() {
        assertThat(resolver.resolve(base, RECORD, "fish_biometry .csv").isPresent(), equalTo(true));
    }

    @Test
    void refusesParentTraversal() {
        assertThat(resolver.resolve(base, RECORD, "../other/c.csv"), is(Optional.empty()));
    }

    @Test
    void refusesAbsolutePath() {
        assertThat(resolver.resolve(base, RECORD, "/etc/passwd"), is(Optional.empty()));
    }

    @Test
    void refusesRecordIdTraversal() {
        assertThat(resolver.resolve(base, "../other", "c.csv"), is(Optional.empty()));
    }

    @Test
    void refusesDirectory() {
        assertThat(resolver.resolve(base, RECORD, "sub"), is(Optional.empty()));
    }

    @Test
    void refusesMissingFile() {
        assertThat(resolver.resolve(base, RECORD, "missing.csv"), is(Optional.empty()));
    }

    @Test
    void refusesNullOrBlankPath() {
        assertThat(resolver.resolve(base, RECORD, null), is(Optional.empty()));
        assertThat(resolver.resolve(base, RECORD, "  "), is(Optional.empty()));
    }

    @Test
    void refusesSymlinkPointingOutsideRecordDirectory() {
        Path link = symlink(base.resolve(RECORD).resolve("escape.csv"), base.resolve("other").resolve("c.csv"));

        assertThat(resolver.resolve(base, RECORD, link.getFileName().toString()), is(Optional.empty()));
    }

    @Test
    void refusesSymlinkedDirectoryPointingOutsideRecordDirectory() {
        symlink(base.resolve(RECORD).resolve("elsewhere"), base.resolve("other"));

        assertThat(resolver.resolve(base, RECORD, "elsewhere/c.csv"), is(Optional.empty()));
    }

    @Test
    void allowsSymlinkToAnotherFileInRecordDirectory() {
        symlink(base.resolve(RECORD).resolve("alias.csv"), base.resolve(RECORD).resolve("sub").resolve("b.csv"));

        assertThat(resolver.resolve(base, RECORD, "alias.csv").isPresent(), equalTo(true));
    }

    @Test
    void returnsRealPath() throws IOException {
        symlink(base.resolve(RECORD).resolve("alias.csv"), base.resolve(RECORD).resolve("a.csv"));

        assertThat(resolver.resolve(base, RECORD, "alias.csv"),
            is(Optional.of(base.resolve(RECORD).resolve("a.csv").toRealPath())));
    }

    @Test
    void refusesSymlinkedRecordDirectoryOutsideBase(@TempDir Path outside) throws IOException {
        Files.writeString(outside.resolve("d.csv"), "secret\n");
        symlink(base.resolve("linked-record"), outside);

        assertThat(resolver.resolve(base, "linked-record", "d.csv"), is(Optional.empty()));
    }

    @Test
    void refusesRecordDirectoryLinkedToBase() throws IOException {
        Files.writeString(base.resolve("top.csv"), "secret\n");
        symlink(base.resolve("loop"), base);

        assertThat(resolver.resolve(base, "loop", "top.csv"), is(Optional.empty()));
    }

    @Test
    void refusesBrokenSymlink() {
        symlink(base.resolve(RECORD).resolve("dangling.csv"), base.resolve(RECORD).resolve("gone.csv"));

        assertThat(resolver.resolve(base, RECORD, "dangling.csv"), is(Optional.empty()));
    }

    private static Path symlink(Path link, Path target) {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (IOException | UnsupportedOperationException e) {
            Assumptions.abort("Symbolic links are not supported here: " + e);
            return link;
        }
    }

    @Test
    void refusesNullBase() {
        assertThat(resolver.resolve(null, RECORD, "a.csv"), is(Optional.empty()));
    }
}
