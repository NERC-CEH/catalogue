<#if licences?has_content>
  dcterms:license <#t>
  <#list licences as licence>
    <#assign licenceUri = uriNormaliser.normalise(licence.uri!"")>
    <#if licenceUri?has_content>
      <${licenceUris.canonicalise(licenceUri)}>
    <#elseif licence.value?has_content>
      ${licenceUris.mintLicence(licence.value)}
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
  <#elseif accessLimitation.value?has_content>
    dcterms:accessRights ${licenceUris.mintAccessRights(accessLimitation.value)} ;
  </#if>
</#if>
