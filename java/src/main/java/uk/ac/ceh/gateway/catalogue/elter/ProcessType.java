package uk.ac.ceh.gateway.catalogue.elter;

public enum ProcessType {
    Simulation("http://www.opengis.net/def/waterml/2.0/processType/Simulation"),
    ManualMethod("http://www.opengis.net/def/waterml/2.0/processType/ManualMethod"),
    Sensor("http://www.opengis.net/def/waterml/2.0/processType/Sensor"),
    Algorithm("http://www.opengis.net/def/waterml/2.0/processType/Algorithm"),
    Unknown("http://www.opengis.net/def/waterml/2.0/processType/unknown");


    ProcessType(String url) {
        this.url = url;
    }

    private final String url;

    public String url () { return url; }
}