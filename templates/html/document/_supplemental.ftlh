<#import "../../functions.ftlh" as func>

<#if supplemental?has_content>
  <div id="section-supplemental">
    <h2>Supplemental information</h2>

    <#list supplemental as supplement>
      <@func.displaySupplemental supplement true/>
    </#list>

  </div>
</#if>

