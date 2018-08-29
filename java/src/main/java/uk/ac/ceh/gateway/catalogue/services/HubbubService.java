package uk.ac.ceh.gateway.catalogue.services;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import lombok.Data;

import lombok.SneakyThrows;
import lombok.val;

public class HubbubService {
  private final WebResource resource;
  private final String username;
  private final String password;

  private static void disableSslVerification() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager(){
          @Override public X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        }
      };
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    }
}

public HubbubService(WebResource resource, String username, String password) {
  this.resource = resource;
  this.username = username;
  this.password = password;
  disableSslVerification();
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
  public JsonNode get(String path) {
    return authenticated(
        ()
            -> resource.path(path)
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .type(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", String.format("Bearer %s", accessToken))
                   .get(JsonNode.class));
  }

  @SneakyThrows
  public JsonNode delete(String path) {
    return authenticated(
        ()
            -> resource.path(path)
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .type(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", String.format("Bearer %s", accessToken))
                   .delete(JsonNode.class));
  }

  @SneakyThrows
  public JsonNode post(String path) {
    return authenticated(
        ()
            -> resource.path(path)
                   .accept(MediaType.APPLICATION_JSON_TYPE)
                   .type(MediaType.APPLICATION_JSON_TYPE)
                   .header("Authorization", String.format("Bearer %s", accessToken))
                   .post(JsonNode.class));
  }

  @SneakyThrows
  public JsonNode patch(String path, String request) {
    return authenticated(
        ()
            -> resource.path(path)
                  .accept(MediaType.APPLICATION_JSON_TYPE)
                  .type(new MediaType("application", "json-patch+json"))
                  .method("PATCH", JsonNode.class, request)
    );
  }

  @Data
  class Token {
    private String access_token;
    private String refresh_token;
  }
}