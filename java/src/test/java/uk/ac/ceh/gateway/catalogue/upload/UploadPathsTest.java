package uk.ac.ceh.gateway.catalogue.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadPathsTest {
    private static final String ID = "c5db2755-bdbb-470f-987b-da71d9489fd0";

    @TempDir Path root;

    private Path folder() {
        return root.toAbsolutePath().normalize().resolve(ID);
    }

    @Test
    void resolvesAFileInsideTheRecordFolder() {
        assertEquals(folder().resolve("data.csv"), UploadPaths.within(root.toString(), ID, "data.csv"));
    }

    @Test
    void resolvesASubfolderAndNormalisesDotSegmentsThatStayInside() {
        assertEquals(folder().resolve("b/data.csv"), UploadPaths.within(root.toString(), ID, "a/../b/./data.csv"));
    }

    @Test
    void joinsAnAbsoluteNameAsARelativeOne() {
        assertEquals(folder().resolve("etc/passwd"), UploadPaths.within(root.toString(), ID, "/etc/passwd"));
    }

    @Test
    void refusesParentSegmentsThatLeaveTheFolder() {
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), ID, "../other/data.csv"));
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), ID, "a/../../other/data.csv"));
    }

    @Test
    void refusesASiblingFolderThatSharesThePrefix() {
        // startsWith on Path compares whole name elements, so <id>-other is not inside <id>
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), ID, "../" + ID + "-other/data.csv"));
    }

    @Test
    void refusesTheRecordFolderItself() {
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), ID, "."));
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), ID, "a/.."));
    }

    @Test
    void refusesAnIdThatLeavesOrIsTheRoot() {
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), "..", "data.csv"));
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), ".", "data.csv"));
        assertThrows(UploadException.class, () -> UploadPaths.within(root.toString(), "a/../..", "data.csv"));
    }
}
