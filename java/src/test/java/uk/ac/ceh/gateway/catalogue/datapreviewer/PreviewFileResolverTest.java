package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
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
    void resolvesFileInRecordDirectory() {
        assertThat(resolver.resolve(base, RECORD, "a.csv"),
            is(Optional.of(base.resolve(RECORD).resolve("a.csv"))));
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
    void refusesNullBase() {
        assertThat(resolver.resolve(null, RECORD, "a.csv"), is(Optional.empty()));
    }
}
