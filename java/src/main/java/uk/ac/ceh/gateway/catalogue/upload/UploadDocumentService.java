package uk.ac.ceh.gateway.catalogue.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static java.lang.String.format;

@Slf4j
@ToString
public class UploadDocumentService {
  private final HubbubService hubbubService;
  // TODO: Only one folder is now used so this can be simplified. The folder is mounted in docker so could be a hardwired location
  private final Map<String, File> folders;
  private final ExecutorService threadPool;

  private static final String[] VISIBLE_STATUS = new String[]{"VALID", "MOVING_FROM", "MOVING_TO", "VALIDATING_HASH", "NO_HASH", "WRITING", "REMOVED_UNKNOWN", "ZIPPED_UNKNOWN", "ZIPPED_UNKNOWN_MISSING", "MOVED_UNKNOWN", "MOVED_UNKNOWN_MISSING", "UNKNOWN", "UNKNOWN_MISSING", "MISSING", "MISSING_UNKNOWN", "CHANGED_MTIME", "CHANGED_HASH", "INVALID", "MOVING_FROM_ERROR", "MOVING_TO_ERROR"};

  public UploadDocumentService(HubbubService hubbubService, Map<String, File> folders, ExecutorService threadPool) {
    this.hubbubService = hubbubService;
    this.folders = folders;
    this.threadPool = threadPool;
    log.info("Creating {}", this);
  }

  private UploadFile convertJSONToUploadFile(String folder, JsonNode item) {
    val uploadFile = new UploadFile();
      val path = item.get("path").asText();
      uploadFile.setPath(path);
      val name = path.replace(format("/%s/", folder), "");
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
    val folder = format("%s/%s", directory, id);
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

    val dropboxRes = hubbubService.get(format("/dropbox/%s", id), documentsPage, VISIBLE_STATUS);
    val dropbox = (ArrayNode) dropboxRes.get("data");
    dropbox.forEach(dropboxFiles::add);
    document.getUploadFiles().put("documents", getUploadFiles("dropbox", id, dropboxFiles, (ObjectNode) dropboxRes.get("pagination")));

    val eidchubRes = hubbubService.get(format("/eidchub/%s", id), datastorePage, VISIBLE_STATUS);
    val eidchub = (ArrayNode) eidchubRes.get("data");
    eidchub.forEach(eidchubFiles::add);
    val eidchubUploadFiles = getUploadFiles("eidchub", id, eidchubFiles, (ObjectNode) eidchubRes.get("pagination"));
    document.getUploadFiles().put("datastore", eidchubUploadFiles);

    val supportingDocumentRes = hubbubService.get(format("/supporting-documents/%s", id), supportingDocumentsPage, VISIBLE_STATUS);
    val supportingDocument = (ArrayNode) supportingDocumentRes.get("data");
    supportingDocument.forEach(supportingDocumentFiles::add);
    document.getUploadFiles().put("supporting-documents", getUploadFiles("supporting-documents", id, supportingDocumentFiles, (ObjectNode) supportingDocumentRes.get("pagination")));
    log.debug("Getting {} for {}. Pages (documents={}, datastore={}, supportingDocuments={})", document, id, documentsPage, datastorePage, supportingDocumentsPage);
    return document;
  }

  @SneakyThrows
  public void getCsv(PrintWriter writer, String id) {
    log.debug("Getting CSV for {}", id);
    val first = hubbubService.get(id);
    val total = first.get("pagination").get("total").asInt();
    val eidchub = (ArrayNode) hubbubService.get(format("/eidchub/%s", id), 1, total).get("data");
    eidchub.forEach(item -> {
      val status = item.get("status").asText();
      if (status.equals("VALID")) {
        val path = item.get("path").asText().replace(format("/eidchub/%s/", id), "");
        val hash = item.get("hash").asText();
        writer.append(format("%s,%s", path, hash));
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

    val dropboxRes = hubbubService.get(format("/dropbox/%s", id), documentsPage);
    val dropbox = (ArrayNode) dropboxRes.get("data");
    dropbox.forEach(dropboxFiles::add);
    document.getUploadFiles().put("documents", getUploadFiles("dropbox", id, dropboxFiles, (ObjectNode) dropboxRes.get("pagination")));
    
    val eidchubRes = hubbubService.get(format("/eidchub/%s", id), datastorePage);
    val eidchub = (ArrayNode) eidchubRes.get("data");
    eidchub.forEach(eidchubFiles::add);
    val eidchubUploadFiles = getUploadFiles("eidchub", id, eidchubFiles, (ObjectNode) eidchubRes.get("pagination"));
    document.getUploadFiles().put("datastore", eidchubUploadFiles);

    val supportingDocumentRes = hubbubService.get(format("/supporting-documents/%s", id), supportingDocumentsPage);
    val supportingDocument = (ArrayNode) supportingDocumentRes.get("data");
    supportingDocument.forEach(supportingDocumentFiles::add);
    document.getUploadFiles().put("supporting-documents", getUploadFiles("supporting-documents", id, supportingDocumentFiles, (ObjectNode) supportingDocumentRes.get("pagination")));
    log.debug("Getting {} for {}", document, id);
    return document;
  }

  public UploadDocument add(String id, String filename, MultipartFile f) {
    log.debug("Adding {} to {}", filename, id);
    threadPool.execute(() -> {
      try (InputStream in = f.getInputStream()) {
        val directory = folders.get("documents");
        val path = directory.getPath() + "/" + id + "/" + filename;
        val file = new File(path);
        file.setReadable(true);
        file.setWritable(false, true);
        file.setExecutable(false);
        writing(id, format("/dropbox/%s/%s", id, filename), in.available());
        FileUtils.copyInputStreamToFile(in, file);
        accept(id, format("/dropbox/%s/%s", id, filename));
        validateFile(id, format("/dropbox/%s/%s", id, filename));
      } catch (IOException err) {
        // TODO: check why this exception is being swallowed
        log.error(format("Error adding file (id=%s filename=%s)", id, filename), err);
      }
    });
    return get(id);
  }

  public UploadDocument delete(String id, String filename) {
    log.debug("Deleting {} from {}", filename, id);
    hubbubService.delete(filename);
    return get(id);
  }

  public UploadDocument accept(String id, String filename) {
    log.debug("Accepting {} for {}", filename, id);
    if (!filename.startsWith("/")) filename = "/" + filename;
    hubbubService.post("/accept", filename);
    return get(id);
  }

  private void writing(String id, String filename, int size) {
    log.debug("Writing {} with size ({}) for {}", filename, size, id);
    if (!filename.startsWith("/")) filename = "/" + filename;
    hubbubService.postQuery("/writing", filename, "size", format("%d", size));
  }

  public UploadDocument validate(String id) {
    log.debug("Validating {}",  id);
    hubbubService.postQuery("/validate", id, "force", "true");
    return get(id);
  }

  public UploadDocument validateFile(String id, String filename) {
    log.debug("Validating {} for {}", filename, id);
    hubbubService.postQuery("/validate", filename, "force", "true");
    return get(id);
  }


  public UploadDocument cancel(String id, String filename) {
    log.debug("Cancelling {} for {}", filename, id);
    threadPool.execute(() -> hubbubService.post("/cancel", filename));
    return get(id);
  }

  public UploadDocument move(String id, String filename, String to) {
    log.debug("Moving {} for {} to {}", filename, id, to);
    threadPool.execute(() -> hubbubService.postQuery("/move", filename, "to", to));
    return get(id);
  }

  public UploadDocument moveToDataStore(String id) {
    log.debug("Moving to DataStore {}", id);
    threadPool.execute(() -> hubbubService.post("/move_all", id));
    return get(id);
  }
}