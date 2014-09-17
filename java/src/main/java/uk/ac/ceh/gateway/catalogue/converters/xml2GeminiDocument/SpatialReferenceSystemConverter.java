package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;

public class SpatialReferenceSystemConverter {
    
    private static final String SPATIAL_REFERENCE_SYSTEM_CODE = "/*/gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier/*/gmd:code/gco:CharacterString";
    private static final String SPATIAL_REFERENCE_SYSTEM_CODESPACE = "/*/gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier/*/gmd:codeSpace/gco:CharacterString";
    private final XPathExpression spatialReferenceSystemCode, spatialReferenceSystemCodeSpace;
    
    public SpatialReferenceSystemConverter(XPath xpath) throws XPathExpressionException{
        this.spatialReferenceSystemCode = xpath.compile(SPATIAL_REFERENCE_SYSTEM_CODE);
        this.spatialReferenceSystemCodeSpace = xpath.compile(SPATIAL_REFERENCE_SYSTEM_CODESPACE);
    }
    
    public SpatialReferenceSystem convert(Document document) throws XPathExpressionException{
        String code = spatialReferenceSystemCode.evaluate(document);
        String codeSpace = spatialReferenceSystemCodeSpace.evaluate(document);
        SpatialReferenceSystem toReturn = SpatialReferenceSystem
                .builder()
                .code(code)
                .codeSpace(codeSpace)
                .build();
        if(toReturn.isEmpty()){
            return null;
        }else{
            return toReturn;
        }
    }
}
