package uk.ac.ceh.gateway.catalogue.converters;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxyException;
import uk.ac.ceh.gateway.catalogue.model.UpstreamInvalidMediaTypeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@ToString
public class TransparentProxyMessageConverter implements HttpMessageConverter<TransparentProxy> {
    private final ClientHttpRequestFactory requestFactory;

    public TransparentProxyMessageConverter(ClientHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        log.info("Creating");
    }

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
        return Arrays.asList(MediaType.ALL);
    }

    @Override
    public TransparentProxy read(Class<? extends TransparentProxy> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("Can't read", inputMessage);
    }

    /**
     * Take a TransparentProxy which represents an upstream uri and transparently
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
        ClientHttpRequest req = requestFactory.createRequest(request.getUri(), HttpMethod.GET);
        try (ClientHttpResponse response = req.execute()) {
            String proxyMediaType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            MediaType desired = request.getDesiredMediaType();

            if(desired == null || desired.isCompatibleWith(MediaType.parseMediaType(proxyMediaType))) {
                outputMessage.getHeaders().set(HttpHeaders.CONTENT_TYPE, proxyMediaType);
                copyAndClose(response.getBody(), outputMessage);
            }
            else {
                throw new UpstreamInvalidMediaTypeException("The proxied server did not return data in the correct media type", null, null, request);
            }
        }
        catch(IOException io) {
            throw new TransparentProxyException("Failed to obtain data from the proxy server", io, null, request);
        }
        catch(InvalidMediaTypeException ex) {
            throw new UpstreamInvalidMediaTypeException("The proxied server returned a media type which could not be read", ex, null, request);
        }
    }

    protected void copyAndClose(InputStream in, HttpOutputMessage response) throws IOException {
        try (OutputStream out = response.getBody()) {
            in.transferTo(out);
        }
    }
}
