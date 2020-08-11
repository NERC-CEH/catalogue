package uk.ac.ceh.gateway.catalogue.services;

import java.util.function.Supplier;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import lombok.SneakyThrows;
import lombok.val;

public class HubbubService {
  private final WebResource resource;

  public HubbubService(WebResource resource) {
    this.resource = resource;
  }

  @SneakyThrows
  public JsonNode get(String path, Integer page, Integer size, String[] status) {
    WebResource query = resource.path("/");
    for (val s : status)
      query = query.queryParam("status", s);

    return query.queryParam("data", "true")
        .queryParam("path", path)
        .queryParam("size", size.toString())
        .queryParam("page", page.toString())
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .get(JsonNode.class);
  }

  @SneakyThrows
  public JsonNode get(String path, Integer page, Integer size) {
    return get(path, page, size, new String[0]);
  }

  @SneakyThrows
  public JsonNode get(String path, Integer page, String[] status) {
    return get(path, page, 20, status);
  }

  @SneakyThrows
  public JsonNode get(String path, Integer page) {
    return get(path, page, 20, new String[0]);
  }

  @SneakyThrows
  public JsonNode get(String path) {
    return get(path, 1, 20, new String[0]);
  }

  @SneakyThrows
  public JsonNode delete(String path) {
    return resource.path("/delete")
        .queryParam("path", path)
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .delete(JsonNode.class);
  }

  @SneakyThrows
  public JsonNode post(String url, String path) {
    return resource.path(url)
        .queryParam("path", path)
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .post(JsonNode.class);
  }

  @SneakyThrows
  public JsonNode postQuery(String url, String path, String queryKey, String queryValue) {
    return resource.path(url)
        .queryParam("path", path)
        .queryParam(queryKey, queryValue)
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .post(JsonNode.class);
  }
}