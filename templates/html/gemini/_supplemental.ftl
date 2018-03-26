<#if supplemental?has_content>
<#assign dataPapers = func.filter(supplemental, "type", "dataPaper")>
<#assign citedBy = func.filter(supplemental, "type", "isCitedBy")>
<#assign supOther = func.filter(supplemental, "type", "website") + func.filter(supplemental, "type", "") + func.filter(supplemental, "type", "relatedArticle") + func.filter(supplemental, "type", "relatedDataset")>

  <div id="section-supplemental">
    <h3>Supplemental information</h3>
    
    <#if dataPapers?has_content>
      <div class="supplemental-block">
        <h4>Data papers that describe this ${resourceType.value}:</h4>
        <#list dataPapers as supplement>
        ${func.displaySupplemental(supplement)}
        </#list>
      </div>
    </#if>

    <#if citedBy?has_content>
      <div class="supplemental-block">
        <h4>This ${resourceType.value} is cited by:</h4>
        <#list citedBy as supplement>
         ${func.displaySupplemental(supplement)}
        </#list>
      </div>
    </#if>

    <#if supOther?has_content || website?has_content>
      <div class="supplemental-block">
        <h4>Other useful information regarding this ${resourceType.value}:</h4>
        <#if supOther?has_content >
          <#list supOther as supplement>
              ${func.displaySupplemental(supplement true)}
          </#list>
        </#if>
      </div>
    </#if>
  
  </div>
</#if>