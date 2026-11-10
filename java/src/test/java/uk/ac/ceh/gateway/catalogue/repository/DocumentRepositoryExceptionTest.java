package uk.ac.ceh.gateway.catalogue.repository;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DocumentRepositoryExceptionTest {

    @Test
    void notFoundWhenTheDatastoreReportsAMissingFile() {
        var ex = new DocumentRepositoryException("Cannot read file: x", new GitFileNotFoundException("x"));
        assertThat(ex.isNotFound(), is(true));
    }

    @Test
    void notNotFoundForOtherReadFaults() {
        var ex = new DocumentRepositoryException("Cannot read file: x", new IOException("pack file corrupt"));
        assertThat(ex.isNotFound(), is(false));
    }

    @Test
    void notFoundIsRecognisedAnywhereInTheCauses() {
        assertThat(DocumentRepositoryException.isNotFound(
            new RuntimeException(new GitFileNotFoundException("x"))), is(true));
        assertThat(DocumentRepositoryException.isNotFound(
            new RuntimeException(new IOException("x"))), is(false));
    }

    // CrowdPermissionService wraps the missing .meta read this way
    @Test
    void notFoundThroughAPermissionCheck() {
        assertThat(DocumentRepositoryException.isNotFound(
            new PermissionDeniedException("No document found for: x", new GitFileNotFoundException("x"))), is(true));
    }

    @Test
    void nullIsNotNotFound() {
        assertThat(DocumentRepositoryException.isNotFound(null), is(false));
    }
}
