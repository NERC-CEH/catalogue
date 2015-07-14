<#if resourceStatus?has_content && resourceStatus == "historicalArchive">
  <div id="not-current" role="alert">
    This ${resourceType.value?html} has been withdrawn.
    <#if revised??>
        The current ${resourceType.value?html} is <a href="${revised.href?html}">${revised.title?html}</a>.
    </#if>
  </div>
</#if>
