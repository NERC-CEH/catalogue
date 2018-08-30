package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.commons.io.FileUtils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.services.HubbubService;

@AllArgsConstructor
public class UploadDocumentService {
  private final HubbubService hubbubService;
  private final Map<String, File> folders;

  private UploadFiles getUploadFiles(String directory, String id) {
    val data = (ArrayNode) hubbubService.get(String.format("%s/%s", directory, id));
    val files = new UploadFiles();
    val documents = files.getDocuments();
    val invalid = files.getInvalid();
    data.forEach(item -> {
      val uploadFile = new UploadFile();
      val path = item.get("path").asText();
      uploadFile.setName(item.get("name").asText());
      uploadFile.setPath(path);
      uploadFile.setPhysicalLocation(item.get("physicalLocation").asText());
      uploadFile.setId(item.get("id").asText());
      uploadFile.setFormat(item.get("format").asText());
      uploadFile.setMediatype(item.get("mediatype").asText());
      uploadFile.setEncoding("utf-8");
      uploadFile.setBytes(item.get("bytes").asLong());
      uploadFile.setHash(item.get("hash").asText());
      if (item.has("destination"))
        uploadFile.setDestination(item.get("destination").asText());
      val status = item.get("status").asText();
      if (status.equals("VALID"))
        documents.put(path, uploadFile);
      else {
        UploadType type = UploadType.INVALID;
        if (status.equals("MISSING"))
          type = UploadType.MISSING_FILE;
        else if (status.equals("UNKNOWN_FILE"))
          type = UploadType.UNKNOWN_FILE;
        else if (status.equals("CHANGED"))
          type = UploadType.INVALID_HASH;

        uploadFile.setType(type);
        invalid.put(path, uploadFile);
      }
    });
    return files;
  }

  public UploadDocument get(String id) {
    val document = new UploadDocument();
    document.setId(id);

    val eidchubFiles = getUploadFiles("eidchub", id);
    document.getUploadFiles().put("datastore", eidchubFiles);
    val isZipped = eidchubFiles.getDocuments().keySet().contains(
        String.format("/mnt/eidchub/%s/%s.zip", id, id));
    eidchubFiles.setZipped(isZipped);

    val dropboxFiles = getUploadFiles("dropbox", id);
    document.getUploadFiles().put("documents", dropboxFiles);

    val ploneFiles = getUploadFiles("supporting-documents", id);
    document.getUploadFiles().put("plone", ploneFiles);

    return document;
  }

  @SneakyThrows
  public UploadDocument add(String id, String filename, InputStream in) {
    val directory = folders.get("documents");
    val file = new File(directory.getPath() + "/" + id + "/" + filename);
    FileUtils.copyInputStreamToFile(in, file);
    return get(id);
  }

  public UploadDocument delete(String id, String filename) {
    hubbubService.delete(filename);
    return get(id);
  }

  public UploadDocument accept(String id, String filename) {
    hubbubService.post(String.format("/accept%s", filename));
    return get(id);
  }

  public UploadDocument zip(String id) {
    hubbubService.post(String.format("/zip/%s", id));
    return get(id);
  }

  public UploadDocument unzip(String id) {
    hubbubService.post(String.format("/unzip/%s", id));
    return get(id);
  }

  public UploadDocument move(String id) {
    hubbubService.post(String.format("/dropbox/%s", id));
    return get(id);
  }

  public UploadDocument setDestination(String id, String filename, String to) {
    hubbubService.patch(String.format("/dropbox/%/%s", id, filename),
        String.format("[{\"op\":\"add\",\"path\":\"/destination\",\"value\":\"%s\"}]", to));
    return get(id);
  }
}