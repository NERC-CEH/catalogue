<#assign infoResources = func.filterRegex(func.filter(doc.onlineResources, "function", "information"), "url", "http://eidc.ceh.ac.uk/metadata")>

<#assign dataPapers = func.filter(doc.supplemental, "type", "dataPaper")>
<#assign citedBy = func.filter(doc.supplemental, "type", "isCitedBy")>

<#if doc.revisionOfIdentifier?has_content || infoResources?has_content || dataPapers?has_content || citedBy?has_content>
  <relatedIdentifiers>
    
    <#if infoResources?has_content>
      <#list infoResources as infoResource>
        <relatedIdentifier relatedIdentifierType="URL" relationType="IsDescribedBy" resourceTypeGeneral="Text">${infoResource.url}</relatedIdentifier>
      </#list>
    </#if>

    <#if doc.revisionOfIdentifier?has_content>
      <relatedIdentifier relatedIdentifierType="DOI" relationType="IsNewVersionOf" resourceTypeGeneral="Dataset">10.5285/${doc.revisionOfIdentifier}</relatedIdentifier>   
    </#if>

    <#if dataPapers?has_content>
      <#list dataPapers as link>
        <#if link.url?matches("^http(|s)://(|dx.)doi.org/10.\\d{2,9}/.+$")>
          <#assign idtype="DOI", uri=link.url?replace("http://dx.doi.org/","")?replace("http://doi.org/","")?replace("https://doi.org/","")>
        <#else>
          <#assign idtype="URL", uri=link.url>
        </#if>
        <relatedIdentifier relatedIdentifierType="${idtype}" relationType="IsDescribedBy" resourceTypeGeneral="DataPaper">${uri}</relatedIdentifier>
      </#list>
    </#if>

    <#if citedBy?has_content>
      <#list citedBy as link>
        <#if link.url?matches("^http(|s)://(|dx.)doi.org/10.\\d{2,9}/.+$")>
          <#assign idtype="DOI", uri=link.url?replace("http://dx.doi.org/","")?replace("http://doi.org/","")?replace("https://doi.org/","")>
        <#else>
          <#assign idtype="URL", uri=link.url>
        </#if>
        <relatedIdentifier relatedIdentifierType="${idtype}" relationType="IsCitedBy" resourceTypeGeneral="Text">${uri}</relatedIdentifier>
      </#list>
    </#if>
  </relatedIdentifiers>
</#if>