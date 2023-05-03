package uk.ac.ceh.gateway.catalogue.ogc;

import lombok.Data;

import java.io.Serializable;

@Data
public class Layer implements Serializable {
    static final long serialVersionUID = 42L;
    private String name, title, legendUrl;
}
