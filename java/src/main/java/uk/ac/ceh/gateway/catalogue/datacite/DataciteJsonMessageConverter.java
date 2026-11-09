package uk.ac.ceh.gateway.catalogue.datacite;

import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATACITE_JSON_VALUE;

/**
 * Writes a {@link DataciteRequest} as {@code application/vnd.api+json}, DataCite's JSON:API type, for
 * the getDoi endpoint. Nothing else is written under that type.
 * <p>
 * The general JSON converter no longer claims every {@code +json} type. When it did, a record
 * requested as {@code application/vnd.api+json} came back as its internal JSON labelled as a DataCite
 * payload (dri-one #409). Keeping the type on this converter, which only writes DataCite requests,
 * means records answer that type with 406.
 */
public class DataciteJsonMessageConverter extends JacksonJsonHttpMessageConverter {

    public DataciteJsonMessageConverter(JsonMapper mapper) {
        super(mapper);
        setSupportedMediaTypes(List.of(MediaType.parseMediaType(DATACITE_JSON_VALUE)));
    }

    @Override
    public boolean canRead(ResolvableType type, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(ResolvableType type, Class<?> valueClass, MediaType mediaType) {
        return DataciteRequest.class.isAssignableFrom(valueClass) && super.canWrite(type, valueClass, mediaType);
    }
}
