package uk.ac.ceh.gateway.catalogue.gemini.elements;

public class XPaths {
    public static final String ID = "/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";
    public static final String TITLE = "/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
    public static final String ALTERNATE_TITLE = "/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString";
    
    public static final String LANGUAGE_CODE_LIST = "/gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeList";
    public static final String LANGUAGE_CODE_LIST_VALUE = "/gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue";
    public static final String TOPIC_CATEGORIES = "/gmd:MD_Metadata/gmd:identificationInfo/gmd:topicCategory/gmd:MD_TopicCategoryCode";
}
