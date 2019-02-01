<#if supplemental?has_content>
  <div id="section-supplemental">
    <h3>Supplemental information</h3>
    
    <#if dataPapers?has_content>
      <div class="supplemental-block" id="dataPapers">
        <h4>Data papers that describe this ${recordType}:</h4>
        <#list dataPapers as supplement>
        ${func.displaySupplemental(supplement)}
        </#list>
      </div>
    </#if>

    <#if referencedBy?has_content>
      <div class="supplemental-block" id="referencedIn">
        <h4>This ${recordType} is referenced in:</h4>
        <#list referencedBy+dataPapers as supplement>
         ${func.displaySupplemental(supplement)}
        </#list>
      </div>
    </#if>

    <#if supOther?has_content || website?has_content>
      <div class="supplemental-block">
        <h4>Other useful information regarding this ${recordType}:</h4>
        <#if supOther?has_content >
          <#list supOther as supplement>
              ${func.displaySupplemental(supplement true)}
          </#list>
        </#if>
      </div>
    </#if>
  
  </div>
</#if>