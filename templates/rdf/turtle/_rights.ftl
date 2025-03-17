<#if licences?has_content>
  dcterms:license <#t>
  <#list licences as licence>
    <#if licence.uri?has_content>
      <#if licence.uri?contains("/licences/OGL/")>
        <https://spdx.org/licenses/OGL-UK-3.0.ttl>
      <#else>
        <${licence.uri?trim}>
      </#if>
    <#elseif licence.value?has_content>
      [ a dcterms:LicenseDocument;
      rdfs:label <@displayLiteral licence.value />;
      ]
    </#if>
  <#sep>,</#sep>
  </#list>;
</#if>

<#--copyright-->
<#assign copyrights = filter(useConstraints, "code", "copyright"  ) >
<#if copyrights?has_content>
  dcterms:rights <#t>
  <#list copyrights as copyright>
      [ a dcterms:RightsStatement ;
      odrs:copyrightNotice  <@displayLiteral copyright.value?replace("©","copyright") />;
      ]
  <#sep>,</#sep>
  </#list>;
</#if>

<#if accessLimitation?has_content>
  <#if accessLimitation.uri?has_content>
    dcterms:accessRights <${accessLimitation.uri?trim}>;
  <#else>
    dcterms:accessRights [
      a dcterms:RightsStatement ;
      rdfs:label <@displayLiteral accessLimitation.value/>
    ] ;
  </#if>
</#if>
