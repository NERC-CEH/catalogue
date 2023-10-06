package uk.ac.ceh.gateway.catalogue.services.post;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PosterTest {

    @Test
    void doPost() {
        Poster poster = new Poster();
        poster.doPost();
        System.out.println("done something");
    }
}