package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.MetadataPreconditionRequiredException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("IfMatchRevision")
class IfMatchRevisionTest {

    @Test
    public void nullHeaderThrowsPreconditionRequired() {
        assertThrows(MetadataPreconditionRequiredException.class, () ->
            IfMatchRevision.require(null));
    }

    @Test
    public void blankHeaderThrowsPreconditionRequired() {
        assertThrows(MetadataPreconditionRequiredException.class, () ->
            IfMatchRevision.require("   "));
    }

    @Test
    public void quotedHeaderIsUnquoted() {
        assertThat(IfMatchRevision.require("\"rev1\""), equalTo("rev1"));
    }

    @Test
    public void unquotedHeaderIsReturnedAsIs() {
        assertThat(IfMatchRevision.require("rev1"), equalTo("rev1"));
    }
}
