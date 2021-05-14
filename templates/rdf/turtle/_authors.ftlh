<#list authors as author>
  [
  <#if author.individualName?has_content>
    a vcard:Individual ;
    vcard:n "${author.individualName}" ;
    <#if author.organisationName?has_content>vcard:organization-name "${author.organisationName}" ;
    </#if>
  <#else>
    a vcard:Organization ;
    vcard:fn "${author.organisationName}" ;
  </#if>
  <#if author.email?has_content>vcard:hasEmail "${author.email}" ;</#if>
  <#if author.nameIdentifier?has_content && author.nameIdentifier?starts_with("http")>
    vcard:hasUID <${author.nameIdentifier}> ;
  </#if>
  ]<#sep>,
</#list>
