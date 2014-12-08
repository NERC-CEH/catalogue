<#if resourceStatus?has_content && resourceStatus == "historicalArchive">
  <div id="not-current" role="alert">
    <#if revised??>
        This ${resourceType?html} is deprecated, the current ${resourceType?html} is <a href="${revised.href?html}">${revised.title?html}</a>.
    <#else>
        This ${resourceType?html} is deprecated.
    </#if>
  </div>
</#if>