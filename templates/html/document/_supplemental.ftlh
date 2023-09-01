<#import "../../functions.ftlh" as func>

<#if supplemental?has_content>
  <#assign displaySupplemental = 1>
  <div id="section-supplemental">
    <h2>Supplemental information <a name="supplemental"></a></h2>

    <#list supplemental as supplement>
      <@func.displaySupplemental supplement true/>
    </#list>

  </div>
</#if>

