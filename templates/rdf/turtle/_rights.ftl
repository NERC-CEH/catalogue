<#--
  Each of the three blocks below uses the <#list ...><#items> form rather than
  <#if x?has_content><#list x as ...>. The body of a <#list> with no <#items>
  match never renders, so a collection whose every entry is filtered out emits
  no predicate at all — where the <#if>/<#sep> form would emit the predicate
  and then a dangling separator with nothing between the commas.
-->
<#list (licences![])?filter(l -> uriNormaliser.normalise(l.uri!"")?has_content || l.value?has_content)>
  dcterms:license <#t>
  <#items as licence>
    <#assign licenceUri = uriNormaliser.normalise(licence.uri!"")>
    <#if licenceUri?has_content>
      <${licenceUris.canonicalise(licenceUri)}>
    <#else>
      ${licenceUris.mintLicence(licence.value)}
    </#if>
  <#sep>,</#sep>
  </#items>;
</#list>

<#--
  copyright. A notice with no text is filtered out rather than minted: the node
  would carry an empty odrs:copyrightNotice and stand for nothing, the empty-node
  problem dri-one #322 found among grants. rightsDetail filters identically, so
  the two cannot disagree about which notices have a node.
-->
<#list (copyrights![])?filter(c -> c.value?has_content)>
  dcterms:rights <#t>
  <#items as copyright>
    ${licenceUris.mintCopyright(copyright.value)}
  <#sep>,</#sep>
  </#items>;
</#list>

<#if accessLimitation?has_content>
  <#assign accessRightsUri = uriNormaliser.normalise(accessLimitation.uri!"")>
  <#if accessRightsUri?has_content>
    dcterms:accessRights <${accessRightsUri}> ;
  <#elseif accessLimitation.value?has_content>
    dcterms:accessRights ${licenceUris.mintAccessRights(accessLimitation.value)} ;
  </#if>
</#if>
