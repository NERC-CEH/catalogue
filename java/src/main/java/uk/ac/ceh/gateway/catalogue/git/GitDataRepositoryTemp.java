package uk.ac.ceh.gateway.catalogue.git;

import com.google.common.eventbus.EventBus;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import uk.ac.ceh.components.datastore.*;
import uk.ac.ceh.components.datastore.git.*;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserBuilderFactory;
import uk.ac.ceh.components.userstore.UserStore;

/**
 * The following is a concrete implementation of a DataRepository. It is based
 * upon the software version control system Git.
 *
 * DataAuthors of this implementation come from some given userstore. Since
 * it is a valid situation that users can be deleted from the userstore but
 * still have revision history associated to them, a UserBuilderFactory needs to
 * be provided which can create DataAuthors are of type A.
 * @author cjohn
 */
public class GitDataRepositoryTemp<A extends DataAuthor & User> extends GitDataRepository {
    private final Repository repository;
    private final UserStore<A> authorResolver;
    private final UserBuilderFactory<A> phantomUserFactory;

    /**
     * The following is the constructor for the GitDataRepository.
     *
     * @param data A folder which contains a git repository
     * @param authorResolver A userStore which will be the primary source for
     *  obtaining GitAuthorUsers
     * @param phantomUserFactory The userbuilder factory which will be used to create
     *  phantom users, that is. Users who do not exist in the UserStore but have
     *  revision history associated to them
     * @param events
     * @throws IOException
     */
    public GitDataRepositoryTemp(File data, UserStore<A> authorResolver,
                             UserBuilderFactory<A> phantomUserFactory,
                             EventBus events) throws IOException {
        super(data, authorResolver, phantomUserFactory, events);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.authorResolver = authorResolver;
        this.phantomUserFactory = phantomUserFactory;
        repository = builder.setGitDir(new File(data, ".git"))
            .readEnvironment() // scan environment GIT_* variables
            .findGitDir() // scan up the file system tree
            .build();

        if(!repository.getDirectory().exists()) { //If the repository does not already exist
            repository.create();                  //Create it
        }
    }

    /**
     * The following method will return a list of revisions for a given filename.
     * The revisions will be ordered in the list so that the first element is the
     * most modern revision of the file and the last the the initial revision of
     * the file.
     * @param name of file to get history for
     * @return A list of revisions ordered as specified above.
     * @throws DataRepositoryException
     */
    @Override public List<DataRevision<A>> getRevisions(String name) throws DataRepositoryException {
        String msg = "%s History debug: %s";
        System.out.println(String.format(msg, LocalDateTime.now(), "enter getRevisions method"));
        System.out.println(String.format(msg, LocalDateTime.now(), "create DataRevision arraylist"));
        List<DataRevision<A>> toReturn = new ArrayList<>();
        System.out.println(String.format(msg, LocalDateTime.now(), "create git repository"));
        Git git = new Git(repository);
        System.out.println(String.format(msg, LocalDateTime.now(), "resolve revision to head"));
        ObjectId revision = resolveRevision(Constants.HEAD);
        if(revision != null) { //Only perform the git log if the repo has a HEAD
            try {
                System.out.println(String.format(msg, LocalDateTime.now(), "create log command"));
                LogCommand logCommand = git.log()
                    .setMaxCount(4)
                    .add(revision)
                    .addPath(name);
                System.out.println(String.format(msg, LocalDateTime.now(), "start loop through commit"));
                for(RevCommit commit : logCommand.call()) {
                    System.out.println(String.format(msg, LocalDateTime.now(), "get author"));
                    A author = getAuthor(commit.getAuthorIdent());
                    System.out.println(String.format(msg, LocalDateTime.now(), "add revision to DataRevision arraylist"));
                    toReturn.add(new GitDataRevision<>(author, commit));
                    System.out.println(String.format(msg, LocalDateTime.now(), "revision info: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(commit.getCommitTime() * 1000L)) + " " + commit.getFullMessage()));
                }
                System.out.println(String.format(msg, LocalDateTime.now(), "end loop through commit and return"));
                return toReturn;
            } catch(IOException | GitAPIException | UnknownUserException ex) {
                System.out.println(String.format(msg, LocalDateTime.now(), "exception occur: " + ex.getMessage()));
                throw new DataRepositoryException(ex);
            }
        }
        else {
            System.out.println(String.format(msg, LocalDateTime.now(), "The repository has no head"));
            throw new GitRevisionNotFoundException("The repository has no head");
        }
    }

    private A getAuthor(PersonIdent authorIdent) throws UnknownUserException {
        String username = authorIdent.getName();
        return (authorResolver.userExists(username))
            ? authorResolver.getUser(username)
            : phantomUserFactory.newUserBuilder(username)
            .set(UserAttribute.EMAIL, authorIdent.getEmailAddress())
            .build();
    }

    /* Resolve the given git revision but wrap exceptions as DataRepositoryExceptions */
    private ObjectId resolveRevision(String revisionStr) throws DataRepositoryException {
        try {
            return repository.resolve(revisionStr);
        }
        catch(AmbiguousObjectException | IncorrectObjectTypeException | RevisionSyntaxException ex) {
            throw new GitRevisionNotFoundException("Failed to find the specified revision", ex);
        }
        catch(IOException ex) {
            throw new DataRepositoryException(ex);
        }
    }
}

