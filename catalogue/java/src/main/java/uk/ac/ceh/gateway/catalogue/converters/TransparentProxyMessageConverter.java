package uk.ac.ceh.gateway.catalogue.converters;

import uk.ac.ceh.gateway.catalogue.model.TransparentProxyException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.model.UpstreamInvalidMediaTypeException;

/**
 * @author cjohn
 */
@Data
public class TransparentProxyMessageConverter implements HttpMessageConverter<TransparentProxy> {
    private final CloseableHttpClient httpClient;
    
    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return clazz.equals(TransparentProxy.class);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public TransparentProxy read(Class<? extends TransparentProxy> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("Can't read"); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Take a TransparentProxy which respresents an upstream uri and transparently
     * proxy it on the the http output message
     * 
     * If supplied, a desired media type will be validated. If that is not present, 
     * the proxying will occur regardless. If it is present then the upstream responses
     * content type will be compared to ensure that it is compatible. If not an
     * UpstreamInvalidMediaTypeException
     * @param request
     * @param contentType
     * @param outputMessage
     * @throws IOException
     * @throws HttpMessageNotWritableException 
     */
    @Override
    public void write(TransparentProxy request, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        HttpGet httpget = new HttpGet(request.getUri());
        try (CloseableHttpResponse response = httpClient.execute(httpget)) {
            HttpEntity entity = response.getEntity();
            
            String proxyMediaType = entity.getContentType().getValue();
            MediaType desired = request.getDesiredMediaType();
            
            if(desired == null || desired.isCompatibleWith(MediaType.parseMediaType(proxyMediaType))) {
                outputMessage.getHeaders().set("Content-Type", proxyMediaType);
                copyAndClose(entity, outputMessage);
            }
            else {
                throw new UpstreamInvalidMediaTypeException("The proxied server did not return data in the correct media type", request);
            }
        }
        catch(IOException io) {
            throw new TransparentProxyException("Failed to obtain data from the proxy server", io, request);
        }
        catch(InvalidMediaTypeException ex) {
            throw new UpstreamInvalidMediaTypeException("The proxied server returned a media type which could not be read", ex, request);
        }
    }
    
    protected void copyAndClose(HttpEntity in, HttpOutputMessage response) throws IOException {
        try (OutputStream out = response.getBody()) {
            in.writeTo(out);
        }
    }
}