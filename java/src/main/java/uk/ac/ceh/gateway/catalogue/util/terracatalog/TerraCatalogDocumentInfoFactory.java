package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

/**
 * The following interface dictates how to generate a the document info for some
 * GeminiDocument. The document info stores application specific data which can't
 * be stored inside a geminidocument directly
 * @author cjohn
 * @param <M> The type of document info which this factory generates
 */
public interface TerraCatalogDocumentInfoFactory<M> {
    /**
     * Given some gemini document and the terracatalogextension generate an 
     * application specific DocumentInfo
     * @param document The gemini document which this document info should 
     *  generated form. This is provided as a curtesy. Most of the information 
     *  which is needed for generating should be obtainable from the TerraCatalogExt
     * @param ext The terra catalog extension file, this will hold most of the 
     *  information required for creating M
     * @return A Document Info for the given GeminiDocument
     */
    M getDocumentInfo(GeminiDocument document, TerraCatalogExt ext);
}
