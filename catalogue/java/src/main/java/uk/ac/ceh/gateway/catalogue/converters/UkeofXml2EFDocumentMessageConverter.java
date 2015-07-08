package uk.ac.ceh.gateway.catalogue.converters;

import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;

/**
 *
 * @author cjohn
 */
public class UkeofXml2EFDocumentMessageConverter extends Jaxb2HttpMessageConverter {
    public UkeofXml2EFDocumentMessageConverter()  {
        super("http://www.ukeof.org.uk/schema/1","http://www.ukeof.org.uk/schema/1/UKEOF_v1.0.xsd");
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(BaseMonitoringType.class);
    }
}