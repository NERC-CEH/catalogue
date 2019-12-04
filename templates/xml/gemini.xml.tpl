<#setting number_format="0">
<#setting date_format = 'yyyy-MM-dd'>
<#compress>
<#escape x as x?xml>

<#if resourceType.value??>
  <#if resourceType.value == "signpost">
    <#assign recordType="dataset">
  <#else> 
    <#assign recordType=resourceType.value>
  </#if>
</#if>

<?xml version="1.0" encoding="UTF-8"?>
<gmd:MD_Metadata xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xlink="http://www.w3.org/1999/xlink">
  <gmd:fileIdentifier><gco:CharacterString>${id}</gco:CharacterString></gmd:fileIdentifier>
  <gmd:language>
    <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/php/code_list.php" codeListValue="eng">eng</gmd:LanguageCode>
  </gmd:language>
  <gmd:characterSet>
    <gmd:MD_CharacterSetCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_CharacterSetCode" codeListValue="utf8">utf8</gmd:MD_CharacterSetCode>
  </gmd:characterSet>
  <#if recordType??>
    <gmd:hierarchyLevel>
      <gmd:MD_ScopeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="${recordType}">${recordType}</gmd:MD_ScopeCode>
    </gmd:hierarchyLevel>
  </#if>
  <gmd:contact>
    <gmd:CI_ResponsibleParty>
      <gmd:organisationName><gco:CharacterString>NERC Environmental Information Data Centre</gco:CharacterString></gmd:organisationName>
      <gmd:contactInfo>
        <gmd:CI_Contact>
          <gmd:address>
            <gmd:CI_Address>              
              <gmd:deliveryPoint><gco:CharacterString>Lancaster Environment Centre,  Library Avenue, Bailrigg</gco:CharacterString></gmd:deliveryPoint>
              <gmd:city><gco:CharacterString>Lancaster</gco:CharacterString></gmd:city>
              <gmd:postalCode><gco:CharacterString>LA1 4AP</gco:CharacterString></gmd:postalCode>
              <gmd:country><gco:CharacterString>UK</gco:CharacterString></gmd:country>
              <gmd:electronicMailAddress><gco:CharacterString>eidc@ceh.ac.uk</gco:CharacterString></gmd:electronicMailAddress>
            </gmd:CI_Address>
          </gmd:address>
        </gmd:CI_Contact>
      </gmd:contactInfo>
      <gmd:role>
        <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="pointOfContact">pointOfContact</gmd:CI_RoleCode>
      </gmd:role>
    </gmd:CI_ResponsibleParty>
  </gmd:contact>
  <gmd:dateStamp>
    <gco:DateTime>${metadataDateTime}</gco:DateTime>
  </gmd:dateStamp>
  <gmd:metadataStandardName>
    <gco:CharacterString>ISO 19115 (UK GEMINI)</gco:CharacterString>
  </gmd:metadataStandardName>
  <gmd:metadataStandardVersion>
    <gco:CharacterString>1.0 (2.2)</gco:CharacterString>
  </gmd:metadataStandardVersion>
  <#if spatialReferenceSystems??>
    <#list spatialReferenceSystems as SRS>
    <gmd:referenceSystemInfo>
      <gmd:MD_ReferenceSystem>
        <gmd:referenceSystemIdentifier>
          <gmd:RS_Identifier>
            <gmd:code><gco:CharacterString>${SRS.code}</gco:CharacterString></gmd:code>
            <gmd:codeSpace><gco:CharacterString>${SRS.codeSpace}</gco:CharacterString></gmd:codeSpace>
            <#if SRS.version?has_content>
              <gmd:version><gco:CharacterString>${SRS.version}</gco:CharacterString></gmd:version>
            </#if>
          </gmd:RS_Identifier>
        </gmd:referenceSystemIdentifier>
      </gmd:MD_ReferenceSystem>
    </gmd:referenceSystemInfo>
    </#list>
  </#if>
  <#--identificationInfo -->
  <#include "gemini/_identificationInfo.xml.tpl">
  <#--distributionInfo -->
  <#include "gemini/_distributionInfo.xml.tpl">
  <#--dataQualityInfo-->
  <#include "gemini/_dataQualityInfo.xml.tpl">
</gmd:MD_Metadata>
</#escape>
</#compress>
