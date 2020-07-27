<#if doc.onlineResources?has_content>
  <#assign infoResources = func.filterRegex(func.filter(doc.onlineResources, "function", "information"), "url", "^https://data-package.ceh.ac.uk/sd/.+")>
</#if>
<#if doc.incomingRelationships?has_content>
  <#assign rel_supersededBy = func.filter(doc.incomingRelationships, "rel", "https://vocabs.ceh.ac.uk/eidc#supersedes")>
</#if>
<#if doc.relatedRecords?has_content>
  <#assign rel_supersedes = func.filter(doc.relatedRecords, "rel", "https://vocabs.ceh.ac.uk/eidc#supersedes")>
</#if>
<#if doc.supplemental?has_content>
  <#assign referencedBy = func.filter(doc.supplemental, "function", "isReferencedBy")>
  <#assign supplementTo = func.filter(doc.supplemental, "function", "isSupplementTo")>
</#if>

<#if supplementTo?has_content || infoResources?has_content || referencedBy?has_content || rel_supersededBy?has_content || rel_supersedes?has_content>
  <relatedIdentifiers>
    
    <#if infoResources?has_content>
      <#list infoResources as infoResource>
        <relatedIdentifier relatedIdentifierType="URL" relationType="IsDescribedBy" resourceTypeGeneral="Text">${infoResource.url}</relatedIdentifier>
      </#list>
    </#if>

    <#if rel_supersedes?has_content && rel_supersedes?size gt 0>
      <#list rel_supersedes as item>
        <relatedIdentifier relatedIdentifierType="DOI" relationType="IsNewVersionOf" resourceTypeGeneral="Dataset">${item.href?replace("https://catalogue.ceh.ac.uk/id/","10.5285/")}</relatedIdentifier>  
      </#list> 
    </#if>

    <#if rel_supersededBy?has_content && rel_supersededBy?size gt 0>
      <#list rel_supersededBy as item>
        <relatedIdentifier relatedIdentifierType="DOI" relationType="IsPreviousVersionOf" resourceTypeGeneral="Dataset">${item.href?replace("https://catalogue.ceh.ac.uk/id/","10.5285/")}</relatedIdentifier>  
      </#list> 
    </#if>

    <#if referencedBy?has_content>
      <#list referencedBy as link>
        <#if link.url?matches("^http(|s)://(|dx.)doi.org/10.\\d{2,9}/.+$")>
          <#assign idtype="DOI", uri=link.url?replace("http://dx.doi.org/","")?replace("http://doi.org/","")?replace("https://doi.org/","")>
        <#else>
          <#assign idtype="URL", uri=link.url>
        </#if>
        <relatedIdentifier relatedIdentifierType="${idtype}" relationType="IsReferencedBy" resourceTypeGeneral="Text">${uri}</relatedIdentifier>
      </#list>
    </#if>

    <#if supplementTo?has_content>
      <#list supplementTo as link>
        <#if link.url?matches("^http(|s)://(|dx.)doi.org/10.\\d{2,9}/.+$")>
          <#assign idtype="DOI", uri=link.url?replace("http://dx.doi.org/","")?replace("http://doi.org/","")?replace("https://doi.org/","")>
        <#else>
          <#assign idtype="URL", uri=link.url>
        </#if>
        <relatedIdentifier relatedIdentifierType="${idtype}" relationType="IsSupplementTo" resourceTypeGeneral="Text">${uri}</relatedIdentifier>
      </#list>
    </#if>
    
  </relatedIdentifiers>
</#if>