package uk.ac.ceh.gateway.catalogue.converters;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;

@Slf4j
@ToString
public class UkeofXml2EFDocumentMessageConverter extends Jaxb2HttpMessageConverter {
    public UkeofXml2EFDocumentMessageConverter()  {
        super("http://www.ukeof.org.uk/schema/1","http://www.ukeof.org.uk/schema/1/UKEOF_v1.0.xsd");
        log.info("Creating {}", this);
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(BaseMonitoringType.class);
    }
}