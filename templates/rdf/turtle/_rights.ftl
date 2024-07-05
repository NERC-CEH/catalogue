<#if licences?has_content>
  dct:license
  <#list licences as licence>
    <#if licence.uri?has_content>
      <${licence.uri?trim}>
    <#elseif licence.value?has_content>
      [ a dct:LicenseDocument;
      rdfs:label "${licence.value}";
      ]
    </#if>
  <#sep>,</#sep>
  </#list>;
</#if>

<#if accessLimitation?has_content>
    dct:accessRights [ a dct:RightsStatement ;
      odrs:attributionText "${accessLimitation.value}" ;
      <#if accessLimitation.uri?has_content>odrs:attributionUrl <${accessLimitation.uri?trim}> </#if>
      ] ;
</#if>
