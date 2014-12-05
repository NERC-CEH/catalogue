<#if resourceStatus?has_content && resourceStatus == "historicalArchive">
  <div id="not-current">
    <strong>Note</strong> This ${resourceType?html} is deprecated.
    <#if revised??>
    The current ${resourceType?html} is <a href="${revised.href?html}">${revised.title?html}</a>.
    </#if>
  </div>
</#if>