<#if licences?has_content>
  dcterms:license <#t>
  <#list licences as licence>
    <#assign licenceUri = uriNormaliser.normalise(licence.uri!"")>
    <#if licenceUri?has_content>
      <#if licenceUri?contains("/licences/ogl/")>
        <https://spdx.org/licenses/OGL-UK-3.0.ttl>
      <#else>
        <${licenceUri}>
      </#if>
    <#elseif licence.value?has_content>
      [ a dcterms:LicenseDocument;
      rdfs:label <@displayLiteral licence.value?replace("\n", " ") />;
      ]
    </#if>
  <#sep>,</#sep>
  </#list>;
</#if>

<#--copyright-->
<#if useConstraints?has_content>
  <#if copyrights?has_content>
    dcterms:rights <#t>
    <#list copyrights as copyright>
        [ a dcterms:RightsStatement ;
        odrs:copyrightNotice  <@displayLiteral copyright.value?replace("©","copyright")?replace("\n", " ") />;
        ]
    <#sep>,</#sep>
    </#list>;
  </#if>
</#if>

<#if accessLimitation?has_content>
  <#assign accessRightsUri = uriNormaliser.normalise(accessLimitation.uri!"")>
  <#if accessRightsUri?has_content>
    dcterms:accessRights <${accessRightsUri}> ;
  <#else>
    dcterms:accessRights [
      a dcterms:RightsStatement ;
      rdfs:label <@displayLiteral accessLimitation.value/>
    ] ;
  </#if>
</#if>
