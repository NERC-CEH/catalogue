package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.InputStream;
import javax.servlet.ServletInputStream;
import lombok.Data;
import lombok.Delegate;
import lombok.EqualsAndHashCode;

/**
 *
 * @author cjohn
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class DelegatedServletInputStream extends ServletInputStream {        
    @Delegate(types=InputStream.class)
    private final InputStream stream;

    @Delegate(types=ServletInputStream.class, excludes=InputStream.class)
    private ServletInputStream wrapped;
}
