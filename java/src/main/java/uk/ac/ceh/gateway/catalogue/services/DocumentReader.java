package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.StringUtils;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

/**
 * static json MetadataDocument reader
 */
public class DocumentReader<T extends MetadataDocument> {
  private static final String datastore = "/var/ceh-catalogue/datastore";

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

      .put("CompositeFeature", String.format("application/vnd.%s+json", "CompositeFeature"))
      .put("Condition", String.format("application/vnd.%s+json", "Condition"))
      .put("DeploymentRelatedProcessDuration", String.format("application/vnd.%s+json", "DeploymentRelatedProcessDuration"))
      .put("Input", String.format("application/vnd.%s+json", "Input"))
      .put("Manufacturer", String.format("application/vnd.%s+json", "Manufacturer"))
      .put("MonitoringFeature", String.format("application/vnd.%s+json", "MonitoringFeature"))
      .put("ObservableProperty", String.format("application/vnd.%s+json", "ObservableProperty"))
      .put("ObservationPlaceholder", String.format("application/vnd.%s+json", "ObservationPlaceholder"))
      .put("OperatingRange", String.format("application/vnd.%s+json", "OperatingRange"))
      .put("Person", String.format("application/vnd.%s+json", "Person"))
      .put("SampleFeature", String.format("application/vnd.%s+json", "SampleFeature"))
      .put("Sensor", String.format("application/vnd.%s+json", "Sensor"))
      .put("SensorType", String.format("application/vnd.%s+json", "SensorType"))
      .put("SingleSystemDeployment", String.format("application/vnd.%s+json", "SingleSystemDeployment"))
      .put("Stimulus", String.format("application/vnd.%s+json", "Stimulus"))
      .put("SystemCapability", String.format("application/vnd.%s+json", "SystemCapability"))
      .put("SystemProperty", String.format("application/vnd.%s+json", "SystemProperty"))
      .put("TemporalProcedure", String.format("application/vnd.%s+json", "TemporalProcedure"))
      .put("VerticalMonitoringFeature", String.format("application/vnd.%s+json", "VerticalMonitoringFeature"))

      .put("data-type", "application/vnd.data-type+json")
      .build();

  @SneakyThrows
  public T read(String guid, Class<T> clazz) {
    if (StringUtils.isBlank(guid) || !exists(guid)) return null;
    val mapper = new ObjectMapper();
    mapper.registerModule(new GuavaModule());
    val json = new File(datastore, String.format("%s.raw", guid));
    val meta = new File(datastore, String.format("%s.meta", guid));
    val document = mapper.readValue(json, clazz);
    val metadataInfo = mapper.readValue(meta, MetadataInfo.class);
    document.setMetadata(metadataInfo);
    return document;
  }


  @SneakyThrows
  public static JsonNode raw(String guid) {
    if (StringUtils.isBlank(guid) || !exists(guid)) return null;
    val mapper = new ObjectMapper();
    mapper.registerModule(new GuavaModule());
    val json = new File(datastore, String.format("%s.raw", guid));
    val meta = new File(datastore, String.format("%s.meta", guid));
    val doc = (ObjectNode) mapper.readTree(json);
    val info = (ObjectNode) mapper.readTree(meta);
    info.put("rawType", lookup.get(info.get("documentType").asText("application/json")));
    info.remove("permissions");
    doc.set("meta", info);
    return doc;
  }

  public static boolean exists(String guid) {
    val json = new File(datastore, String.format("%s.raw", guid));
    val meta = new File(datastore, String.format("%s.meta", guid));
    return json.exists() && meta.exists();
  }
}