package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Controller
@Slf4j
public class DocumentController {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final DocumentLinkService linkService;
    private final CitationService citationService;
    
    @Autowired
    public DocumentController(  DataRepository<CatalogueUser> repo,
                                DocumentReadingService documentReader,
                                DocumentInfoMapper documentInfoMapper,
                                DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory,
                                BundledReaderService<MetadataDocument> documentBundleReader,
                                DocumentLinkService linkService,
                                CitationService citationService) {
        this.repo = repo;
        this.documentReader = documentReader;
        this.documentInfoMapper = documentInfoMapper;
        this.infoFactory = infoFactory;
        this.documentBundleReader = documentBundleReader;
        this.linkService = linkService;
        this.citationService = citationService;
    }
    
    @PreAuthorize("@permission.toAccess(#file, 'WRITE')")
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
            GeminiDocument data = documentReader.read(Files.newInputStream(tmpFile), contentMediaType, GeminiDocument.class); 
            MetadataInfo metadataDocument = infoFactory.createInfo(data, contentMediaType); //get the metadata info
            
            repo.submitData(data.getId() + ".meta", (o)-> documentInfoMapper.writeInfo(metadataDocument, o) )
                .submitData(data.getId() + ".raw", (o) -> Files.copy(tmpFile, o) )
                .commit(user, commitMessage);
            
        }
        finally {
            Files.delete(tmpFile); //file no longer needed
        }
    }
    
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.GET)   
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file, HttpServletRequest request) throws DataRepositoryException, IOException, UnknownContentTypeException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        
        return readMetadata(user, file, latestRev.getRevisionID(), request);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'DOCUMENT_READ')")
    @RequestMapping(value = "history/{revision}/{file}",
                    method = RequestMethod.GET)
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("revision") String revision,
            HttpServletRequest request) throws DataRepositoryException, IOException, UnknownContentTypeException {
        MetadataDocument document = documentBundleReader.readBundle(file, revision);
        document.attachUri(getCurrentUri(request, file, revision));
        if(document instanceof GeminiDocument) {
            GeminiDocument geminiDocument = (GeminiDocument)document;
            geminiDocument.setDocumentLinks(new HashSet<>(linkService.getLinks(geminiDocument, getLinkUriBuilder(request))));
            geminiDocument.setCitation(citationService.getCitation(geminiDocument));
        }
        log.debug("document requested: {}", document);
        return document;
    }

    @PreAuthorize("@permission.toAccess(#file, 'WRITE')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.DELETE)
    @ResponseBody
    public DataRevision<CatalogueUser> deleteDocument(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "message", defaultValue = "delete document") String reason,
            @PathVariable("file") String file) throws DataRepositoryException, IOException {
        return repo.deleteData(file + ".meta")
                   .deleteData(file + ".raw")
                   .commit(user, reason);
    }
    
    protected URI getCurrentUri(HttpServletRequest request, String file, String revision) throws DataRepositoryException {
        if(revision.equals(repo.getLatestRevision().getRevisionID())) {
           return ServletUriComponentsBuilder
                .fromContextPath(request)
                .path("/documents/")
                .path(file)
                .build()
                .toUri();
        }
        else {
            return ServletUriComponentsBuilder
                .fromContextPath(request)
                .path("/history/")
                .path(revision)
                .path("/")
                .path(file)
                .build()
                .toUri();
        }
    }
    
    private UriComponentsBuilder getLinkUriBuilder(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromContextPath(request).path("/documents/{fileIdentifier}");
    }
}
