package uk.ac.ceh.gateway.catalogue.services;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.WebResource;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class HubbubService {
  private final WebResource resource;
  private final String accessToken;
  private final String refreshToken;

  @SneakyThrows
  public JsonNode get(String path) {
    return resource.path(path)
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", String.format("Bearer %s", accessToken))
        .get(JsonNode.class);
  }
}