package uk.ac.ceh.gateway.catalogue.upload;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ExecutorService threadPool;

  @SneakyThrows
  private UploadFiles getUploadFiles(String directory, String id, ArrayNode data) {
    val folder = String.format("%s/%s", directory, id);
    val files = new UploadFiles();
    val documents = files.getDocuments();
    val invalid = files.getInvalid();
    data.forEach(item -> {
      val uploadFile = new UploadFile();
      val path = item.get("path").asText();
      uploadFile.setPath(path);
      val name = path.replace(String.format("/%s/", folder), "");
      uploadFile.setName(name);
      uploadFile.setId(name.replaceAll("[^\\w?]", "-"));

      if (item.has("time"))
        uploadFile.setTime(item.get("time").asLong());
      if (item.has("physicalLocation"))
        uploadFile.setPhysicalLocation(item.get("physicalLocation").asText());
      if (item.has("format"))
        uploadFile.setFormat(item.get("format").asText());
      if (item.has("mediatype"))
        uploadFile.setMediatype(item.get("mediatype").asText());
      uploadFile.setEncoding("utf-8");
      if (item.has("bytes"))
        uploadFile.setBytes(item.get("bytes").asLong());
      if (item.has("hash"))
        uploadFile.setHash(item.get("hash").asText());
      if (item.has("destination"))
        uploadFile.setDestination(item.get("destination").asText());
      val status = item.get("status").asText();
      uploadFile.setType(status);
      if (status.equals("VALID") || status.equals("VALIDATING_HASH") || status.equals("NO_HASH") || status.equals("MOVING"))
        documents.put(path, uploadFile);
      else if (!status.equals("REMOVED") && !status.equals("MOVED") && !status.equals("ZIPPED")) {
        invalid.put(path, uploadFile);
      }
    });
    return files;
  }

  public UploadDocument get(String id) {
    val document = new UploadDocument();
    document.setId(id);

    val eidchubFiles = new ObjectMapper().createArrayNode();
    val dropboxFiles = new ObjectMapper().createArrayNode();
    val ploneFiles = new ObjectMapper().createArrayNode();

    val data = (ArrayNode) hubbubService.get(id);
    data.forEach(item -> {
      val path = item.get("path").asText();
      if (path.contains("eidchub"))
        eidchubFiles.add(item);
      if (path.contains("dropbox"))
        dropboxFiles.add(item);
      if (path.contains("supporting-documents"))
        ploneFiles.add(item);
    });

    val eidchubUploadFiles = getUploadFiles("eidchub", id, eidchubFiles);
    val isZipped = eidchubUploadFiles.getDocuments().keySet().contains(
        String.format("/eidchub/%s/%s.zip", id, id));
    eidchubUploadFiles.setZipped(isZipped);

    document.getUploadFiles().put("datastore", eidchubUploadFiles);
    document.getUploadFiles().put("documents", getUploadFiles("dropbox", id, dropboxFiles));
    document.getUploadFiles().put("plone", getUploadFiles("supporting-documents", id, ploneFiles));

    return document;
  }

  @SneakyThrows
  public UploadDocument add(String id, String filename, InputStream in) {
    val directory = folders.get("documents");
    val path = directory.getPath() + "/" + id + "/" + filename;
    val file = new File(path);
    file.setReadOnly();
    FileUtils.copyInputStreamToFile(in, file);

    threadPool.execute(() -> {
      accept(id, String.format("/dropbox/%s/%s", id, filename));
      validateFile(id, String.format("/dropbox/%s/%s", id, filename));
    });

    return get(id);
  }

  public UploadDocument delete(String id, String filename) {
    hubbubService.delete(filename);
    return get(id);
  }

  @SneakyThrows
  public UploadDocument accept(String id, String filename) {
    if (!filename.startsWith("/")) filename = "/" + filename;
    hubbubService.post(String.format("/accept%s", filename));
    return get(id);
  }

  @SneakyThrows
  public UploadDocument validate(String id) {
    hubbubService.postQuery(String.format("/validate/%s", id), "force", "true");
    return get(id);
  }

  @SneakyThrows
  public UploadDocument validateFile(String id, String filename) {
    hubbubService.postQuery(String.format("/validate%s", filename), "force", "true");
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

  public UploadDocument move(String id, String filename, String to) {
    hubbubService.postQuery(String.format("/move%s", filename), "to", to);
    return get(id);
  }

  public UploadDocument moveToDataStore(String id) {
    hubbubService.post(String.format("/move_all/%s", id));
    return get(id);
  }
}