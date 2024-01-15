package uk.ac.ceh.gateway.catalogue.converters;

import java.util.Collections;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

/**
 * Hardcode the namespaces you need
 * 
 * @see http://www.ibm.com/developerworks/xml/library/x-nmspccontext/index.html
 */
public class HardcodedOGCNamespaceResolver implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {
        if(prefix == null) {
            throw new IllegalArgumentException("No prefix provided!");
        }
        
        switch(prefix) {
            case "wms": return "http://www.opengis.net/wms";
            case "xlink": return "http://www.w3.org/1999/xlink";
            default:    return "";
        }
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return "";
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return Collections.emptyIterator();
    }
}
