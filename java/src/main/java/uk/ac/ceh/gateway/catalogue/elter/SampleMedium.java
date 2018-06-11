package uk.ac.ceh.gateway.catalogue.elter;

public enum SampleMedium {
    Water("http://www.opengis.net/def/waterml/2.0/medium/Water"),
    GroundWater("http://www.opengis.net/def/waterml/2.0/medium/GroundWater"),
    SurfaceWater("http://www.opengis.net/def/waterml/2.0/medium/SurfaceWater"),
    SedimentWater("http://www.opengis.net/def/waterml/2.0/medium/SedimentWater"),
    PoreWater("http://www.opengis.net/def/waterml/2.0/medium/PoreWater"),
    PoreAir("http://www.opengis.net/def/waterml/2.0/medium/PoreAir"),
    Soil("http://www.opengis.net/def/waterml/2.0/medium/Soil"),
    SoilAir("http://www.opengis.net/def/waterml/2.0/medium/SoilAir"),
    SoilWater("http://www.opengis.net/def/waterml/2.0/medium/SoilWater"),
    Atmosphere("http://www.opengis.net/def/waterml/2.0/medium/Atmosphere"),
    Tissue("http://www.opengis.net/def/waterml/2.0/medium/Tissue"),
    GroundSnow("http://www.opengis.net/def/waterml/2.0/medium/GroundSnow"),
    Unknown("http://www.opengs.net/def/nil/OGC/0/unknown");

    SampleMedium(String url) {
        this.url = url;
    }

    private final String url;

    public String url() {
        return url;
    }
}