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
  private final String username;
  private final String password;

  public HubbubService(WebResource resource, String username, String password) {
    this.resource = resource;
    this.username = username;
    this.password = password;
  }

  private String accessToken = null;
  private String refreshToken = null;

  @SneakyThrows
  public JsonNode authenticated(Supplier<JsonNode> fn) {
    try {
      if (accessToken == null && refreshToken == null)
        updateTokens();
      return fn.get();
    } catch (UniformInterfaceException expiredAccess) {
      if (shouldGetNewToken(expiredAccess)) {
        try {
          updateAccessToken();
        } catch (UniformInterfaceException expiredRefresh) {
          if (shouldGetNewToken(expiredRefresh)) {
            updateTokens();
          }
        }
      }
      return fn.get();
    }
  }

  private boolean shouldGetNewToken(UniformInterfaceException expiredAccess) {
    val res = expiredAccess.getResponse().getEntity(ObjectNode.class);
    val message = res.get("msg").asText();
    return message.equals("Token has expired") || message.equals("Token has been revoked");
  }

  private void updateAccessToken() {
    val token = resource.path("/refresh")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", String.format("Bearer %s", refreshToken))
                    .get(JsonNode.class);
    this.accessToken = token.get("access_token").asText();
  }

  private void updateTokens() {
    ObjectNode credentials = new ObjectMapper().createObjectNode();
    credentials.put("username", username);
    credentials.put("password", password);

    val token = resource.path("/auth")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(JsonNode.class, credentials);
    this.accessToken = token.get("access_token").asText();
    this.refreshToken = token.get("refresh_token").asText();
  }

  @SneakyThrows
  public JsonNode get(String path, Integer page) {
    return authenticated(() -> resource.path(path)
                   .queryParam("data", "true")
                   .queryParam("size", "20")
                   .queryParam("page", page.toString())
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", String.format("Bearer %s", accessToken))
                   .get(JsonNode.class));
  }

  @SneakyThrows
  public JsonNode delete(String path) {
    return authenticated(() -> resource.path(path)
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .type(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", String.format("Bearer %s", accessToken))
                   .delete(JsonNode.class));
  }

  @SneakyThrows
  public JsonNode post(String path) {
    return authenticated(() -> resource.path(path)
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .type(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", String.format("Bearer %s", accessToken))
                   .post(JsonNode.class));
  }

  @SneakyThrows
  public JsonNode postQuery(String path, String queryKey, String queryValue) {
    return authenticated(() -> resource.path(path)
                   .queryParam(queryKey, queryValue)
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .type(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", String.format("Bearer %s", accessToken))
                   .post(JsonNode.class));
  }
}