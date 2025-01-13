<#if licences?has_content>
  dct:license <#t>
  <#list licences as licence>
    <#if licence.uri?has_content>
      <#if licence.uri?contains("/licences/OGL/")>
        <https://spdx.org/licenses/OGL-UK-3.0.ttl>
      <#else>
        <${licence.uri?trim}>
      </#if>
    <#elseif licence.value?has_content>
      [ a dct:LicenseDocument;
      rdfs:label <@displayLiteral licence.value />;
      ]
    </#if>
  <#sep>,</#sep>
  </#list>;
</#if>

<#if accessLimitation?has_content>
    dct:accessRights [ a dct:RightsStatement ;
      odrs:attributionText <@displayLiteral accessLimitation.value /> ;
      <#if accessLimitation.uri?has_content>odrs:attributionUrl <${accessLimitation.uri?trim}> </#if>
      ] ;
</#if>
