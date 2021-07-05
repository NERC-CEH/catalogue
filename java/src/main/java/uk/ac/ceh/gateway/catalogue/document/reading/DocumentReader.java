package uk.ac.ceh.gateway.catalogue.document.reading;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import java.io.File;
import java.util.Map;

import static java.lang.String.format;

//TODO: Remove when satisfied "raw/documents/*" endpoint no longer used
/**
 * static json MetadataDocument reader
 */
@Service
@Slf4j
@ToString
public class DocumentReader<T extends MetadataDocument> {
  private static final String datastore = "/var/ceh-catalogue/datastore";

  public DocumentReader() {
    log.info("Creating {}", this);
  }

  private final static Map<String, String> lookup = ImmutableMap.<String, String>builder()
      .put("GEMINI_DOCUMENT", "application/gemini+json")
      .put("EF_DOCUMENT", "application/monitoring+json")
      .put("IMP_DOCUMENT", "application/model+json")
      .put("CEH_MODEL", "application/vnd.ceh.model+json")
      .put("CEH_MODEL_APPLICATION", "application/vnd.ceh.model.application+json")
      .put("LINK_DOCUMENT", "application/link+json")
      .put("osdp-agent", "application/vnd.osdp.agent+json")
      .put("osdp-dataset", "application/vnd.osdp.dataset+json")
      .put("osdp-model", "application/vnd.osdp.model+json")
      .put("osdp-sample", "application/vnd.osdp.sample+json")
      .put("osdp-publication", "application/vnd.osdp.publication+json")
      .put("osdp-monitoring-activity", "application/vnd.osdp.monitoring-activity+json")
      .put("osdp-monitoring-programme", "application/vnd.osdp.monitoring-programme+json")
      .put("osdp-monitoring-facility", "application/vnd.osdp.monitoring-facility+json")
      .put("sample-archive", "application/vnd.sample-archive+json")
      .put("data-type", "application/vnd.data-type+json")
      .build();

  @SneakyThrows
  public T read(String guid, Class<T> clazz) {
    if (StringUtils.isBlank(guid) || !exists(guid)) return null;
    val mapper = new ObjectMapper();
    mapper.registerModule(new GuavaModule());
    val json = read(guid, "raw");
    val meta = read(guid, "meta");
    val document = mapper.readValue(json, clazz);
    val metadataInfo = mapper.readValue(meta, MetadataInfo.class);
    document.setMetadata(metadataInfo);
    return document;
  }

  public File read(String guid, String extension) {
    return new File(datastore, format("%s.%s", guid, extension));
  }


  @SneakyThrows
  public static JsonNode raw(String guid) {
    if (StringUtils.isBlank(guid) || !exists(guid)) return null;
    val mapper = new ObjectMapper();
    mapper.registerModule(new GuavaModule());
    val json = new File(datastore, format("%s.raw", guid));
    val meta = new File(datastore, format("%s.meta", guid));
    val doc = (ObjectNode) mapper.readTree(json);
    val info = (ObjectNode) mapper.readTree(meta);
    info.put("rawType", lookup.get(info.get("documentType").asText("application/json")));
    info.remove("permissions");
    doc.set("meta", info);
    return doc;
  }

  public static boolean exists(String guid) {
    val json = new File(datastore, format("%s.raw", guid));
    val meta = new File(datastore, format("%s.meta", guid));
    return json.exists() && meta.exists();
  }
}