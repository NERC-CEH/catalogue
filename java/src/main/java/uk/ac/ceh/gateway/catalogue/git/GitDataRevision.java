package uk.ac.ceh.gateway.catalogue.git;

import org.eclipse.jgit.revwalk.RevCommit;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.datastore.DataRevision;

/**
 *
 * @author cjohn
 */
class GitDataRevision<A extends DataAuthor> implements DataRevision<A> {
    private final A author;
    private final RevCommit commit;

    GitDataRevision(A author, RevCommit commit) {
        this.author = author;
        this.commit = commit;
    }

    @Override public String getRevisionID() {
        return commit.getId().getName();
    }

    @Override public String getMessage() {
        return commit.getFullMessage();
    }

    @Override public String getShortMessage() {
        return commit.getShortMessage();
    }

    @Override public A getAuthor() {
        return author;
    }
}

