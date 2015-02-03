<#if doc.resourceStatus?has_content && doc.resourceStatus == "historicalArchive">
  <div id="not-current" role="alert">
    This ${doc.resourceType?html} is deprecated.
    <#if doc.revised??>
        The current ${doc.resourceType?html} is <a href="${doc.revised.href?html}">${doc.revised.title?html}</a>.
    </#if>
  </div>
</#if>