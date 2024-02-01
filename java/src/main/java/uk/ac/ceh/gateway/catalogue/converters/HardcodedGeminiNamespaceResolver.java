package uk.ac.ceh.gateway.catalogue.converters;

import java.util.Collections;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

/**
 * Hardcode the namespaces you need
 *
 * @see http://www.ibm.com/developerworks/xml/library/x-nmspccontext/index.html
 */
public class HardcodedGeminiNamespaceResolver implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {
        if(prefix == null) {
            throw new IllegalArgumentException("No prefix provided!");
        }

        switch(prefix) {
            case "gmd": return "http://www.isotc211.org/2005/gmd";
            case "gco": return "http://www.isotc211.org/2005/gco";
            case "csw": return "http://www.opengis.net/cat/csw/2.0.2";
            case "gml": return "http://www.opengis.net/gml/3.2";
            case "gmx": return "http://www.isotc211.org/2005/gmx";
            case "srv": return "http://www.isotc211.org/2005/srv";
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
