<#if doc.onlineResources?has_content>
<#assign infoResources = func.filterRegex(func.filter(doc.onlineResources, "function", "information"), "url", "https://data-package.ceh.ac.uk/sd/")>
</#if>

<#if doc.supplemental?has_content>
  <#assign referencedBy = func.filter(doc.supplemental, "type", "isReferencedBy")>
  <#assign supplementTo = func.filter(doc.supplemental, "type", "IsSupplementTo")>
</#if>

<#if doc.revisionOfIdentifier?has_content || infoResources?has_content || referencedBy?has_content>
  <relatedIdentifiers>
    
    <#if infoResources?has_content>
      <#list infoResources as infoResource>
        <relatedIdentifier relatedIdentifierType="URL" relationType="IsDescribedBy" resourceTypeGeneral="Text">${infoResource.url}</relatedIdentifier>
      </#list>
    </#if>

    <#if doc.revisionOfIdentifier?has_content>
      <relatedIdentifier relatedIdentifierType="DOI" relationType="IsNewVersionOf" resourceTypeGeneral="Dataset">10.5285/${doc.revisionOfIdentifier}</relatedIdentifier>   
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