package uk.ac.ceh.gateway.catalogue.gemini.elements;

public interface Renderable {

    /**
     * Returns {@code true} if objects fields are not empty.
     * 
     * Convenient method to check if object has anything to render in a template.
     * 
     * @return {@code true} if fields are not empty, otherwise {@code false}
     */
    boolean isRenderable();
}