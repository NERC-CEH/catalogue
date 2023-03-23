<#if deimsSites??>
  <div>
    <h2>DEIMS sites</h2>
    <#list deimsSites as site>
      <a href="${site.url}">${site.title}</a><#sep><br></#sep>
    </#list>
  </div>
</#if>
<#if elterProject??>
  <div>
    <h2>eLTER projects</h2>
    <#list elterProject as project>
      ${project.value}<#sep><br></#sep>
    </#list>
  </div>
</#if>
