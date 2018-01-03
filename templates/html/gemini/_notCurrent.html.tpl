<#if resourceStatus?has_content && resourceStatus == "historicalArchive">
  <div id="not-current" role="alert">
    
    <p>
      <i class="fa fa-exclamation-circle fa-lg"></i> <b>THIS ${resourceType.value?upper_case} HAS BEEN WITHDRAWN</b>
        <#if revised??>
        and superseded by <a href="${revised.href}">${revised.title}</a>
        </#if>
    .</p>

    <#if erratum??>
     <p class="errataDescription">${erratum?html?replace("\n", "<br>")}</p>
    </#if>
    
  </div>
</#if>

