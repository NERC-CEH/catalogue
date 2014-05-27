package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.notSureWhereYet.DataDocumentResource;
import uk.ac.ceh.gateway.catalogue.services.DocumentBundleService;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Controller
public class DocumentController {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentReadingService<Metadata> documentReader;
    private final DocumentInfoMapper<MetadataInfo, Metadata> documentInfoMapper;
    private final DocumentBundleService<Metadata, MetadataInfo, Metadata> documentBundler;
    
    @Autowired
    public DocumentController(  DataRepository<CatalogueUser> repo,
                                DocumentReadingService<Metadata> documentReader,
                                DocumentInfoMapper documentInfoMapper,
                                DocumentBundleService<Metadata, MetadataInfo, Metadata> documentBundler) {
        this.repo = repo;
        this.documentReader = documentReader;
        this.documentInfoMapper = documentInfoMapper;
        this.documentBundler = documentBundler;
    }
    
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST)
    @ResponseBody
    public void uploadDocument(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "message", defaultValue = "new document") String commitMessage,
            HttpServletRequest request,
            @RequestHeader("Content-Type") String contentType) throws DataRepositoryException, IOException, UnknownContentTypeException {
        
        MediaType contentMediaType = MediaType.parseMediaType(contentType);
        Path tmpFile = Files.createTempFile("upload", null); //Create a temp file to upload the input stream to
        try {
            Files.copy(request.getInputStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING); //copy the file so that we can pass over multiple times
            
            //the documentReader will close the underlying inputstream
            Metadata data = documentReader.read(Files.newInputStream(tmpFile), contentMediaType); 
            MetadataInfo metadataDocument = documentInfoMapper.createInfo(data, contentMediaType); //get the metadata info
            
            repo.submitData(filename(data.getId(), "meta"), (o)-> documentInfoMapper.writeInfo(metadataDocument, o) )
                .submitData(filename(data.getId(), "raw"), (o) -> Files.copy(tmpFile, o) )
                .commit(user, commitMessage);
            
        }
        finally {
            Files.delete(tmpFile); //file no longer needed
        }
    }
    
    @RequestMapping( value ="documents/{file}",
                     method = RequestMethod.GET)
    @ResponseBody
    public Object readMetadata(
            @PathVariable("file") String file,
            @RequestHeader("Accept") String acceptType) throws DataRepositoryException, IOException, UnknownContentTypeException {
        List<MediaType> acceptableTypes = MediaType.parseMediaTypes(acceptType);
        MediaType.sortBySpecificityAndQuality(acceptableTypes);
        
        String latestRev = repo.getLatestRevision();
        MetadataInfo documentInfo = documentInfoMapper.readInfo(
                                        repo.getData(latestRev, filename(file, "meta"))
                                            .getInputStream());
        
        DataDocument dataDoc = repo.getData(latestRev, filename(file, "raw"));
        
        if(!acceptableTypes.isEmpty() && acceptableTypes.get(0).includes(documentInfo.getRawMediaType())) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(documentInfo.getRawMediaType());
            return new HttpEntity(new DataDocumentResource(dataDoc), headers);
        }
        else {
            Metadata document = documentReader.read(dataDoc.getInputStream(),
                                                    documentInfo.getRawMediaType());
            documentBundler.bundle(document, documentInfo);
            return document;
        }
    }
    
    @PreAuthorize("@permission.toAccess(#file, 'WRITE')")
    @RequestMapping(value = "documents/{file}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public DataRevision<CatalogueUser> deleteDocument(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "message", defaultValue = "delete document") String reason,
            @PathVariable("file") String file) throws DataRepositoryException, IOException {
        return repo.deleteData(filename(file, "meta"))
                   .deleteData(filename(file, "raw"))
                   .commit(user, reason);
    }
    
    protected String filename(String name, String extension) {
        return String.format("%s.%s", name, extension);
    }
}
