package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import uk.ac.ceh.gateway.catalogue.model.FileChecksum;
import uk.ac.ceh.gateway.catalogue.services.FileUploadService;
import uk.ac.ceh.gateway.catalogue.services.JiraService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UploadController {
    private final FileUploadService fileUploadService;
    private final JiraService jiraService;

    @Autowired
    public UploadController(FileUploadService fileUploadService, JiraService jiraService) {
        this.fileUploadService = fileUploadService;
        this.jiraService = jiraService;
    }

    @RequestMapping(value = "upload/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUpload(@PathVariable("guid") String guid) throws IOException {
        List<FileChecksum> checksums = fileUploadService.getChecksums(guid);

        Map<String, Object> model = new HashMap<>();
        model.put("checksums", checksums);

        return new ModelAndView("/html/documents-upload.html.tpl", model);
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/finish", method = RequestMethod.POST)
    @ResponseBody
    public List<String> finish(@PathVariable("guid") String guid) {
        List<String> list = new ArrayList<String>();
        list.add("done");

        jiraService.getIssue(guid);

        return list;
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/add", method = RequestMethod.POST)
    @ResponseBody
    public List<FileChecksum> documentsUploadSave(@PathVariable("guid") String guid,
            @RequestParam("file") MultipartFile file) throws IOException, NoSuchAlgorithmException {
        try (InputStream in = file.getInputStream()) {
            fileUploadService.uploadData(in, guid, file.getOriginalFilename());
            return fileUploadService.getChecksums(guid);
        }
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "/upload/{guid}/{file:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteFile(@PathVariable("guid") String guid, @PathVariable("file") String file)
            throws IOException {
        fileUploadService.deleteFile(guid, file);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "File already exists")
    @ExceptionHandler(FileAlreadyExistsException.class)
    public void fileAlreadyExists() {
    }
}