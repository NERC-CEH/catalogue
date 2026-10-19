package uk.ac.ceh.gateway.catalogue.wms;

import lombok.Value;

import java.util.List;

/**
 * A local WMS a record page can preview.
 */
@Value
public class WmsMap {
    String serviceId;
    String title;
    /** The stored GetCapabilities URL, on the public host. */
    String capabilitiesUrl;
    /** The capabilities URL without its query, e.g. https://catalogue.ceh.ac.uk/maps/abc. */
    String publicMapUrl;
    /** Same-origin path the browser calls, e.g. /maps/abc. */
    String localPath;
    /** Empty means "all layers". */
    List<String> layerNames;
}
