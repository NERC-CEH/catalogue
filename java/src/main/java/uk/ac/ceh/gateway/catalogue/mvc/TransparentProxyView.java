package uk.ac.ceh.gateway.catalogue.mvc;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.web.servlet.View;

/**
 * A simple spring view which given a url will transparently proxy the response 
 * from that url. Currently only proxies with get requests.
 * @author cjohn
 */
@Data
public class TransparentProxyView implements View {
    private final CloseableHttpClient httpClient;
    private final String url;
    
    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse servletResponse) throws Exception {
        HttpGet httpget = new HttpGet(url);
        
        try (CloseableHttpResponse response = httpClient.execute(httpget)) {
            HttpEntity entity = response.getEntity();
            servletResponse.setContentType(entity.getContentType().getValue());
            copyAndClose(entity, servletResponse);
        }
    }
    
    private static void copyAndClose(HttpEntity in, HttpServletResponse response) throws IOException {
        try (ServletOutputStream out = response.getOutputStream()) {
            in.writeTo(out);
        }
    }
}
