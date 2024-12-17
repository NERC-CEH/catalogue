<#compress>
<#assign
    MD_checks = metadataQuality.check(id)
    problems = MD_checks.getProblems()
    errors = MD_checks.getErrors()
    infos =  MD_checks.getInfo()
    warnings = MD_checks.getWarnings()
    totalIssues = errors + infos +  warnings
    alertclass="warning"
>

<#if totalIssues gte 1>
  <#if errors gte 1 >
      <#assign alertclass="danger">
  <#elseif warnings gte 1 >
      <#assign alertclass="warning" >
  <#elseif infos gte 1 >
      <#assign alertclass="primary">
  </#if>

  <div class="p-3 bg-${alertclass}-subtle rounded-2 my-1">
    METADATA QUALITY <span class="badge rounded-pill bg-${alertclass}">${totalIssues}</span>
  </div>
</#if>
</#compress>
