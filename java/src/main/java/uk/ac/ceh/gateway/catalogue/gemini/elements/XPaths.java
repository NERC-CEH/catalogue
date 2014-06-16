package uk.ac.ceh.gateway.catalogue.gemini.elements;

public class XPaths {
    public static final String ID = "/*/gmd:fileIdentifier/gco:CharacterString";
    public static final String TITLE = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:title/gco:CharacterString";
    public static final String DESCRIPTION = "/*/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString";
    public static final String ALTERNATE_TITLE = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:alternateTitle/gco:CharacterString";
    public static final String LANGUAGE_CODE_LIST = "/*/gmd:identificationInfo/gmd:language/gmd:LanguageCode/@codeList";
    public static final String LANGUAGE_CODE_LIST_VALUE = "/*/gmd:identificationInfo/gmd:language/gmd:LanguageCode/@codeListValue";
    public static final String TOPIC_CATEGORIES = "/*/gmd:identificationInfo/gmd:topicCategory/gmd:MD_TopicCategoryCode";
    public static final String DESCRIPTIVE_KEYWORDS = "/*/gmd:identificationInfo/*/gmd:descriptiveKeywords";
    
}
