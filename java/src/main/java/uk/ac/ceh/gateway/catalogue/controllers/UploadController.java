package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.FileChecksum;
import uk.ac.ceh.gateway.catalogue.services.FileUploadService;
import uk.ac.ceh.gateway.catalogue.services.JiraService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

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
    private final PermissionService permissionservice;

    @Autowired
    public UploadController(FileUploadService fileUploadService, JiraService jiraService,
            PermissionService permissionservice) {
        this.fileUploadService = fileUploadService;
        this.jiraService = jiraService;
        this.permissionservice = permissionservice;
    }

    @RequestMapping(value = "upload/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUpload(@PathVariable("guid") String guid) throws IOException {
        Map<String, Object> model = new HashMap<>();

        boolean isScheduled = false;

        val issues = jiraService.getIssues("eeffacad-1f23-456a-aac0-1bda40958f75");
        String message = "";
        if (issues.size() == 0 || issues.size() > 1)
            message = "if you have any issues please contact an admin";
        else {
            val issue = issues.get(0);
            val status = issue.getStatus();
            isScheduled = status.equals("scheduled");

            if (status.equals("open") || status.equals("approved"))
                message = "awaiting scheduling from admin";
            else if (status.equals("in progress"))
                message = "awaiting approval from admin";
            else if (status.equals("resolved") || status.equals("closed"))
                message = "finsihed";
            else if (status.equals("on hold"))
                message = "plase contact an admin";
        }

        model.put("message", message);

        List<FileChecksum> checksums = fileUploadService.getChecksums(guid);
        model.put("checksums", checksums);

        boolean canUpload = permissionservice.userCanUpload(guid) && isScheduled;
        model.put("canUpload", canUpload);

        return new ModelAndView("/html/documents-upload.html.tpl", model);
    }

    @PreAuthorize("@permission.userCanUpload(#guid)")
    @RequestMapping(value = "upload/{guid}/finish", method = RequestMethod.POST)
    @ResponseBody
    public List<String> finish(@PathVariable("guid") String guid) {
        List<String> list = new ArrayList<String>();
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
    @RequestMapping(value = "upload/{guid}/delete", method = RequestMethod.POST)
    @ResponseBody
    public List<FileChecksum> deleteFile(@PathVariable("guid") String guid, @RequestParam("file") String file)
            throws IOException {
        fileUploadService.deleteFile(guid, file);
        return fileUploadService.getChecksums(guid);
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "File already exists")
    @ExceptionHandler(FileAlreadyExistsException.class)
    public void fileAlreadyExists() {
    }
}