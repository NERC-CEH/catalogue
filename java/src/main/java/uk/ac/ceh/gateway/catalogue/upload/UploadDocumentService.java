package uk.ac.ceh.gateway.catalogue.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@AllArgsConstructor
public class UploadDocumentService {
  private final HubbubService hubbubService;
  // TODO: Only one folder is now used so this can be simplified. The folder is mounted in docker so could be a hardwired location
  private final Map<String, File> folders;
  private final ExecutorService threadPool;

  private static final String[] VISIBLE_STATUS = new String[]{"VALID", "MOVING_FROM", "MOVING_TO", "VALIDATING_HASH", "NO_HASH", "WRITING", "REMOVED_UNKNOWN", "ZIPPED_UNKNOWN", "ZIPPED_UNKNOWN_MISSING", "MOVED_UNKNOWN", "MOVED_UNKNOWN_MISSING", "UNKNOWN", "UNKNOWN_MISSING", "MISSING", "MISSING_UNKNOWN", "CHANGED_MTIME", "CHANGED_HASH", "INVALID", "MOVING_FROM_ERROR", "MOVING_TO_ERROR"};

  private UploadFile convertJSONToUploadFile(String folder, JsonNode item) {
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
    return uploadFile;
  }

  private UploadFiles getUploadFiles(String directory, String id, ArrayNode data, ObjectNode paginationNode) {
    val folder = String.format("%s/%s", directory, id);
    val files = new UploadFiles();
    val pagination = new UploadDocumentPagination();
    pagination.setPage(paginationNode.get("page").asInt());
    pagination.setSize(paginationNode.get("size").asInt());
    pagination.setTotal(paginationNode.get("total").asInt());
    files.setPagination(pagination);
    val documents = files.getDocuments();
    val invalid = files.getInvalid();
    data.forEach(item -> {
      val uploadFile = convertJSONToUploadFile(folder, item);
      val status = item.get("status").asText();
      val path = item.get("path").asText();
      if (status.equals("WRITING") || status.equals("VALID") || status.equals("VALIDATING_HASH") || status.equals("NO_HASH") || status.equals("MOVING_FROM") || status.equals("MOVING_TO"))
        documents.put(path, uploadFile);
      else
        invalid.put(path, uploadFile);
    });
    return files;
  }

  @SneakyThrows
  public UploadDocument get(String id, int documentsPage, int datastorePage, int supportingDocumentsPage) {
    val document = new UploadDocument();
    document.setId(id);

    val eidchubFiles = new ObjectMapper().createArrayNode();
    val dropboxFiles = new ObjectMapper().createArrayNode();
    val supportingDocumentFiles = new ObjectMapper().createArrayNode();

    val dropboxRes = hubbubService.get(String.format("/dropbox/%s", id), documentsPage, VISIBLE_STATUS);
    val dropbox = (ArrayNode) dropboxRes.get("data");
    dropbox.forEach(item -> { dropboxFiles.add(item); });
    document.getUploadFiles().put("documents", getUploadFiles("dropbox", id, dropboxFiles, (ObjectNode) dropboxRes.get("pagination")));

    val eidchubRes = hubbubService.get(String.format("/eidchub/%s", id), datastorePage, VISIBLE_STATUS);
    val eidchub = (ArrayNode) eidchubRes.get("data");
    eidchub.forEach(item -> { eidchubFiles.add(item); });
    val eidchubUploadFiles = getUploadFiles("eidchub", id, eidchubFiles, (ObjectNode) eidchubRes.get("pagination"));
    document.getUploadFiles().put("datastore", eidchubUploadFiles);

    val supportingDocumentRes = hubbubService.get(String.format("/supporting-documents/%s", id), supportingDocumentsPage, VISIBLE_STATUS);
    val supportingDocument = (ArrayNode) supportingDocumentRes.get("data");
    supportingDocument.forEach(item -> { supportingDocumentFiles.add(item); });
    document.getUploadFiles().put("supporting-documents", getUploadFiles("supporting-documents", id, supportingDocumentFiles, (ObjectNode) supportingDocumentRes.get("pagination")));

    return document;
  }

  @SneakyThrows
  public void getCsv(PrintWriter writer, String id) {
    val first = hubbubService.get(id);
    val total = first.get("pagination").get("total").asInt();
    val eidchub = (ArrayNode) hubbubService.get(String.format("/eidchub/%s", id), 1, total).get("data");
    eidchub.forEach(item -> {
      val status = item.get("status").asText();
      if (status.equals("VALID")) {
        val path = item.get("path").asText().replace(String.format("/eidchub/%s/", id), "");
        val hash = item.get("hash").asText();
        writer.append(String.format("%s,%s", path, hash));
        writer.append("\n\r");
      }
    });
  }

  @SneakyThrows
  public UploadDocument get(String id) {
    int documentsPage = 1;
    int datastorePage = 1;
    int supportingDocumentsPage = 1;

    val document = new UploadDocument();
    document.setId(id);

    val eidchubFiles = new ObjectMapper().createArrayNode();
    val dropboxFiles = new ObjectMapper().createArrayNode();
    val supportingDocumentFiles = new ObjectMapper().createArrayNode();

    val dropboxRes = hubbubService.get(String.format("/dropbox/%s", id), documentsPage);
    val dropbox = (ArrayNode) dropboxRes.get("data");
    dropbox.forEach(item -> { dropboxFiles.add(item); });
    document.getUploadFiles().put("documents", getUploadFiles("dropbox", id, dropboxFiles, (ObjectNode) dropboxRes.get("pagination")));
    
    val eidchubRes = hubbubService.get(String.format("/eidchub/%s", id), datastorePage);
    val eidchub = (ArrayNode) eidchubRes.get("data");
    eidchub.forEach(item -> { eidchubFiles.add(item); });
    val eidchubUploadFiles = getUploadFiles("eidchub", id, eidchubFiles, (ObjectNode) eidchubRes.get("pagination"));
    document.getUploadFiles().put("datastore", eidchubUploadFiles);

    val supportingDocumentRes = hubbubService.get(String.format("/supporting-documents/%s", id), supportingDocumentsPage);
    val supportingDocument = (ArrayNode) supportingDocumentRes.get("data");
    supportingDocument.forEach(item -> { supportingDocumentFiles.add(item); });
    document.getUploadFiles().put("supporting-documents", getUploadFiles("supporting-documents", id, supportingDocumentFiles, (ObjectNode) supportingDocumentRes.get("pagination")));

    return document;
  }

  public UploadDocument add(String id, String filename, MultipartFile f) {
    threadPool.execute(() -> {
      try (InputStream in = f.getInputStream()) {
        val directory = folders.get("documents");
        val path = directory.getPath() + "/" + id + "/" + filename;
        val file = new File(path);
        file.setReadable(true);
        file.setWritable(false, true);
        file.setExecutable(false);
        writing(id, String.format("/dropbox/%s/%s", id, filename), in.available());
        FileUtils.copyInputStreamToFile(in, file);
        accept(id, String.format("/dropbox/%s/%s", id, filename));
        validateFile(id, String.format("/dropbox/%s/%s", id, filename));
      } catch (IOException err) {
        System.out.println(err);
      }
    });

    return get(id);
  }

  public UploadDocument delete(String id, String filename) {
    hubbubService.delete(filename);
    return get(id);
  }

  public UploadDocument accept(String id, String filename) {
    if (!filename.startsWith("/")) filename = "/" + filename;
    hubbubService.post("/accept", filename);
    return get(id);
  }

  public UploadDocument writing(String id, String filename, int size) {
    if (!filename.startsWith("/")) filename = "/" + filename;
    hubbubService.postQuery("/writing", filename, "size", String.format("%d", size));
    return get(id);
  }

  public UploadDocument validate(String id) {
    hubbubService.postQuery("/validate", id, "force", "true");
    return get(id);
  }

  public UploadDocument validateFile(String id, String filename) {
    hubbubService.postQuery("/validate", filename, "force", "true");
    return get(id);
  }


  public UploadDocument cancel(String id, String filename) {
    threadPool.execute(() -> {
      hubbubService.post("/cancel", filename);
    });
    return get(id);
  }

  public UploadDocument move(String id, String filename, String to) {
    threadPool.execute(() -> {
      hubbubService.postQuery("/move", filename, "to", to);
    });
    return get(id);
  }

  public UploadDocument moveToDataStore(String id) {
    threadPool.execute(() -> {
      hubbubService.post("/move_all", id);
    });
    return get(id);
  }
}