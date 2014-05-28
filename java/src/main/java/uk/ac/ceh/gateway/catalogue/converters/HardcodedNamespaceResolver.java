package uk.ac.ceh.gateway.catalogue.converters;

import java.util.Collections;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

/**
 * Hardcode the namespaces you need
 * 
 * @see http://www.ibm.com/developerworks/xml/library/x-nmspccontext/index.html
 * @author RJSC
 */
public class HardcodedNamespaceResolver implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {        
        switch(prefix.toLowerCase()) {
            case "gmd": return "http://www.isotc211.org/2005/gmd";
            case "gco": return "http://www.isotc211.org/2005/gco";
            case "csw": return "http://www.opengis.net/cat/csw/2.0.2";
            default:    return "";
        }
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return "";
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return Collections.emptyIterator();
    }
}