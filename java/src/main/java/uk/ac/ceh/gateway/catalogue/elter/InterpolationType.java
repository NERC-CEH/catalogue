package uk.ac.ceh.gateway.catalogue.elter;

public enum InterpolationType {

    Continuous("http://www.opengis.net/def/waterml/2.0/interpolationType/Continuous"),
    Discontinuous("http://www.opengis.net/def/waterml/2.0/interpolationType/Discontinuous"),
    InstantTotal("http://www.opengis.net/def/waterml/2.0/interpolationType/InstantTotal"),
    AveragePrec("http://www.opengis.net/def/waterml/2.0/interpolationType/AveragePrec"),
    MaxPrec("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxPrec"),
    MinPrec("http://www.opengis.net/def/waterml/2.0/interpolationType/MinPrec"),
    TotalPrec("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalPrec"),
    AverageSucc("http://www.opengis.net/def/waterml/2.0/interpolationType/AverageSucc"),
    TotalSucc("http://www.opengis.net/def/waterml/2.0/interpolationType/TotalSucc"),
    MinSucc("http://www.opengis.net/def/waterml/2.0/interpolationType/MinSucc"),
    MaxSucc("http://www.opengis.net/def/waterml/2.0/interpolationType/MaxSucc"),
    ConstPrec("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstPrec"),
    ConstSucc("http://www.opengis.net/def/waterml/2.0/interpolationType/ConstSucc"),
    Statistical("http://www.opengis.net/def/waterml/2.0/interpolationType/Statistical");


    InterpolationType(String url) {
        this.url = url;
    }

    private final String url;

    public String url () { return url; }
}