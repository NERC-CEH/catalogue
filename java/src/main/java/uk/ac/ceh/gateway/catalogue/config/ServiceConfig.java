package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.xml.xpath.XPathExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentBundleService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.JacksonDocumentInfoMapper;

/**
 * The following spring configuration will populate service beans
 * @author cjohn
 */
@Configuration
public class ServiceConfig {
    @Autowired ObjectMapper jacksonMapper;
    
    @Bean
    public DocumentReadingService<GeminiDocument> documentReadingService() throws XPathExpressionException {
        return new MessageConverterReadingService<>(GeminiDocument.class)
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter());
    }
    
    @Bean
    public DocumentInfoFactory<GeminiDocument, MetadataInfo> documentInfoFactory() {
        return (GeminiDocument document, MediaType contentType) -> {
            MetadataInfo toReturn = document.getMetadata();
            
            //If no MetadataInfo is attached to the document, we need to create 
            //a new one. 
            if(toReturn == null) {
                toReturn = new MetadataInfo();
            }
            
            toReturn.setRawType(contentType.toString()); //set the raw type
            return toReturn;
        };
    }
    
    @Bean
    public DocumentInfoMapper documentInfoMapper() {
        return new JacksonDocumentInfoMapper(jacksonMapper, MetadataInfo.class);
    }
    
    @Bean
    public DocumentBundleService<GeminiDocument, MetadataInfo> documentBundleService() {
        return (GeminiDocument document, MetadataInfo info) -> {
            info.hideMediaType();
            document.setMetadata(info);
            return document;
        };
    }
}
