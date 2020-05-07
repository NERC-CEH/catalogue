<#if supplemental?has_content>
  <div id="section-supplemental">
    <h3>Supplemental information</h3>
    
    <#if supplementTo?has_content>
      <div class="supplemental-block" id="supplementTo">
        <h4>This ${recordType} is a supplement to:</h4>
        <#list supplementTo as supplement>
        ${func.displaySupplemental(supplement)}
        </#list>
      </div>
    </#if>

    <#if referencedBy?has_content>
      <div class="supplemental-block" id="referencedIn">
        <h4>This ${recordType} is referenced in:</h4>
        <#list referencedBy as supplement>
         ${func.displaySupplemental(supplement)}
        </#list>
      </div>
    </#if>

    <#if websites?has_content>
      <div class="supplemental-block">
        <h4>Related websites:</h4>
        <#if websites?has_content >
          <#list websites as supplement>
              ${func.displaySupplemental(supplement true)}
          </#list>
        </#if>
      </div>
    </#if>

    <#if supOther?has_content>
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