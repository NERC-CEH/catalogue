package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

/**
 * static json MetadataDocument reader
 */
public class DocumentReader<T extends MetadataDocument> {
  private static final String datastore = "/var/ceh-catalogue/datastore";

  @SneakyThrows
  public T read(String guid, Class<T> clazz) {
    val mapper = new ObjectMapper();
    mapper.registerModule(new GuavaModule());
    val json = new File(datastore, String.format("%s.raw", guid));
    val document = mapper.readValue(json, clazz);
    val meta = new File(datastore, String.format("%s.meta", guid));
    val metadataInfo = mapper.readValue(meta, MetadataInfo.class);
    document.setMetadata(metadataInfo);
    return document;
  }
}