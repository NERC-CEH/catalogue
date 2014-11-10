package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataAuthor;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 * The following utility will populate a data repository with history from Terra
 * Catalog exports.
 * @author cjohn
 * @param <U> The type of user which this import will deal with
 */
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Slf4j
public class TerraCatalogImporter<M, U extends DataAuthor & User> {
    private static final Pattern TC_EXPORT_REGEX = Pattern.compile("BACKUP_TC_[0-9]*-[0-9]*-[0-9]*-[0-9]*-[0-9]*-[0-9]*-[0-9]*\\.zip");
    
    private final DataRepository<U> repo;
    private final DocumentListingService documentList;
    private final TerraCatalogUserFactory<U> userFactory;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<M> documentInfoMapper;
    private final TerraCatalogDocumentInfoFactory<M> metadataDocument;
    private final TerraCatalogExtReader tcExtReader;
    private final U importUser;
    
    public TerraCatalogImporter(DataRepository<U> repo,
                                DocumentListingService documentList,
                                TerraCatalogUserFactory<U> userFactory,
                                DocumentReadingService documentReader,
                                DocumentInfoMapper<M> documentInfoMapper,
                                TerraCatalogDocumentInfoFactory<M> metadataDocument,
                                U importUser) {
        this(repo, documentList, userFactory, documentReader, documentInfoMapper, metadataDocument, new TerraCatalogExtReader(), importUser);
    }
    /**
     * 
     * @param exportDirectory
     * @throws IOException
     * @throws UnknownContentTypeException 
     */
    public void importDirectory(File exportDirectory) throws IOException, UnknownContentTypeException {
        File[] exportFiles = exportDirectory
                .listFiles((File d, String f)-> TC_EXPORT_REGEX.matcher(f).matches());
        Arrays.sort(exportFiles); //Order the terracatalog files lexically, 
        for(File currTCExport: exportFiles) {
            log.info("Importing from: {}", currTCExport.getName());
            importFile(new ZipFile(currTCExport));
        }
    }
    
    public void importFile(ZipFile file) throws IOException, UnknownContentTypeException {
        List<TerraCatalogPair> toImport = getFiles(file);
        if(!toImport.isEmpty()) {
            //Check for files to delete. If there are any, do that now
            List<String> toDelete = getFilesInRepositoryButNotInImport(toImport);
            if(!toDelete.isEmpty()) {
                deleteFiles(file.getName(), toDelete);
               log.info("Deleting: {}", file.getName());
            }
            
            //Group the file pairs by owner
            for(Entry<U, List<TerraCatalogPair>> authorFiles: toImport
                                                                        .stream()
                                                                        .collect(Collectors.groupingBy(TerraCatalogPair::getOwner))
                                                                        .entrySet()) {           
                commitAuthorsFiles(file.getName(), authorFiles.getKey(), authorFiles.getValue());
                
            }
        }
    }
    
    /**
     * Given an author and a user, commit all the users files in one operation
     * @param importFile The name of the file to be imported (for data repo commit)
     * @param author The author of the files
     * @param files The files to commit. There must be at least one file in the list
     * @return The data revision that results from the commit
     * @throws uk.ac.ceh.components.datastore.DataRepositoryException
     */
    protected DataRevision<U> commitAuthorsFiles(String importFile, U author, List<TerraCatalogPair> files) throws DataRepositoryException {
        Queue<TerraCatalogPair> toCommit = new LinkedList<>(files);
        
        TerraCatalogPair firstToCommit = toCommit.poll();
        
        DataOngoingCommit<U> ongoingCommit = repo.submitData(firstToCommit.getId() + ".meta", (o)-> documentInfoMapper.writeInfo(firstToCommit.getInfo(), o) )
                                                 .submitData(firstToCommit.getId() + ".raw", (o) -> IOUtils.copy(firstToCommit.getXmlInputStream(), o) );
        
        for(TerraCatalogPair currToCommit : toCommit) {
            ongoingCommit = ongoingCommit.submitData(currToCommit.getId() + ".meta", (o)-> documentInfoMapper.writeInfo(currToCommit.getInfo(), o) )
                                         .submitData(currToCommit.getId() + ".raw", (o) -> IOUtils.copy(currToCommit.getXmlInputStream(), o) );
        }
        log.info("Commiting");
        return ongoingCommit.commit(author, "Commit by terraCatalog importer for " + author.getEmail() + " from " + importFile);
    }
    
    /**
     * Deletes a list of ids from the toDeleteList
     * @param importFile The import file which doesn't contain toDeleteList
     * @param toDeleteList A list with at least one element in to delete
     * @return The datarevision this delete operation created
     * @throws DataRepositoryException 
     */
    protected DataRevision<U> deleteFiles(String importFile, List<String> toDeleteList) throws DataRepositoryException {
        Queue<String> toDelete = new LinkedList<>(toDeleteList);
        //Delete the first element to get an ongoing commit
        String firstToDelete = toDelete.poll();

        DataOngoingCommit<U> ongoingCommit = repo.deleteData(firstToDelete + ".meta")
                                                 .deleteData(firstToDelete + ".raw");

        //Then delete the rest
        for(String currToDelete : toDelete) {
            ongoingCommit = ongoingCommit.deleteData(currToDelete + ".meta")
                                         .deleteData(currToDelete + ".raw");
        }

        return ongoingCommit.commit(importUser, "Deleted files not present in import: " + importFile);
    }
    
    protected List<String> getFilesInRepositoryButNotInImport(List<TerraCatalogPair> files) throws DataRepositoryException {
        return documentList.filterFilenames(repo.getFiles())
                   .stream()
                   .map((f) -> FilenameUtils.removeExtension(f))
                   .filter((f)-> files.stream().noneMatch((t)-> t.getId().equals(f)))
                   .collect(Collectors.toList());
    }
   
    protected List<TerraCatalogPair> getFiles(ZipFile file) throws IOException, UnknownContentTypeException {
        List<String> zipFileEntry = file
                .stream()
                .filter((e)-> FilenameUtils.isExtension(e.getName(),"tcext"))
                .map((e)-> FilenameUtils.removeExtension(e.getName()))
                .collect(Collectors.toList());
        
        List<TerraCatalogPair> toReturn = new ArrayList<>();
        for(String filename: zipFileEntry) {
            toReturn.add(new TerraCatalogPair(file, filename));
        }
        return toReturn;
    }
       
    @Data
    protected class TerraCatalogPair {
        private final ZipFile file;
        private final String name;
        
        private final GeminiDocument document;
        private final TerraCatalogExt tcExt;
        private final M info;
        private final U owner;
        
        public TerraCatalogPair(ZipFile file, String name) throws IOException, UnknownContentTypeException {
            this.file = file;
            this.name = name;
            
            //Harvest all the data from the parts using the injected dependencies
            this.document = documentReader.read(getXmlInputStream(), MediaType.APPLICATION_XML, GeminiDocument.class);
            this.tcExt = tcExtReader.readTerraCatalogExt(getInputStream("tcext"));
            this.info = metadataDocument.getDocumentInfo(document, tcExt);
            this.owner = userFactory.getAuthor(tcExt);
        }
              
        public String getId() {
            return document.getId();
        }
        
        public final InputStream getXmlInputStream() throws IOException {
            return getInputStream("xml");
        }
        
        private InputStream getInputStream(String ext) throws IOException {
            return file.getInputStream(file.getEntry(name + "." + ext));
        }
    }
}
