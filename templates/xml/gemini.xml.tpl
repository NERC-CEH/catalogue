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
<gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gss="http://www.isotc211.org/2005/gss" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.isotc211.org/2005/gmd http://inspire.ec.europa.eu/draft-schemas/inspire-md-schemas/apiso-inspire/apiso-inspire.xsd">
  <gmd:fileIdentifier><gco:CharacterString>${id}</gco:CharacterString></gmd:fileIdentifier>
  <gmd:language>
    <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/php/code_list.php" codeListValue="eng">English</gmd:LanguageCode>
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
      <gmd:organisationName><gco:CharacterString>Environmental Information Data Centre</gco:CharacterString></gmd:organisationName>
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
      <gmx:Anchor xlink:href="http://vocab.nerc.ac.uk/collection/MD1/current/GEMINI/">UK GEMINI</gmx:Anchor>
  </gmd:metadataStandardName>
  <gmd:metadataStandardVersion>
      <gco:CharacterString>2.3</gco:CharacterString>
  </gmd:metadataStandardVersion>
  <#if spatialReferenceSystems??>
    <#list spatialReferenceSystems as SRS>
    <gmd:referenceSystemInfo>
      <gmd:MD_ReferenceSystem>
        <gmd:referenceSystemIdentifier>
          <gmd:RS_Identifier>
            <gmd:code>
              <#if SRS.code?has_content && SRS.code?starts_with("http")>
                <gmx:Anchor xlink:href="${SRS.code}">${SRS.title}</gmx:Anchor>
              <#else>
                <gco:CharacterString>${SRS.title}</gco:CharacterString>
              </#if>
            </gmd:code>
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
