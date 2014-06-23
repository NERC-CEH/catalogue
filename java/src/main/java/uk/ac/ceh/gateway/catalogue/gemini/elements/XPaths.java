package uk.ac.ceh.gateway.catalogue.gemini.elements;

public class XPaths {
    public static final String ID = "/*/gmd:fileIdentifier/gco:CharacterString";
    public static final String TITLE = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:title/gco:CharacterString";
    public static final String DESCRIPTION = "/*/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString";
    public static final String ALTERNATE_TITLE = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:alternateTitle/gco:CharacterString";
    public static final String LANGUAGE_CODE_LIST = "/*/gmd:identificationInfo/gmd:language/gmd:LanguageCode/@codeList";
    public static final String LANGUAGE_CODE_LIST_VALUE = "/*/gmd:identificationInfo/gmd:language/gmd:LanguageCode/@codeListValue";
    public static final String TOPIC_CATEGORIES = "/*/gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode";
    public static final String DESCRIPTIVE_KEYWORDS = "/*/gmd:identificationInfo/*/gmd:descriptiveKeywords";
    public static final String ORDER_URL = "/*/gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine[*/gmd:function/*/@codeListValue=\"order\"]/*/gmd:linkage/gmd:URL[starts-with(text(), \"http://gateway.ceh.ac.uk/download?fileIdentifier\")]";
    public static final String SUPPORTING_DOCUMENTS_URL = "/*/gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine[*/gmd:function/*/@codeListValue=\"information\"]/*/gmd:linkage/gmd:URL[starts-with(text(), \"http://eidchub.ceh.ac.uk/metadata\")]";
    public static final String LICENCE_URL = "/*/gmd:identificationInfo/*/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor[text() = \"Licence terms and conditions apply\"]/@xlink:href";    
    public static final String OTHER_CITATION_DETAILS = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:otherCitationDetails/gco:CharacterString";
    public static final String RESOURCE_TYPE_CODE_LIST = "/*/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeList";
    public static final String RESOURCE_TYPE_CODE_LIST_VALUE = "/*/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";
}
