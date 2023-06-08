<#if deimsSites?? && deimsSites?size gt 0>
  <div>
    <h2>DEIMS site<#if deimsSites?size gt 1>s</#if></h2>
    <#list deimsSites as site>
      <a href="${site.url}" target="_blank" rel="noopener noreferrer">${site.title}</a><#sep><br></#sep>
    </#list>
  </div>
</#if>
<#if elterProject?? && elterProject?size gt 0>
  <div>
    <h2>eLTER projects</h2>
    <#list elterProject as project>
      ${project.value}<#sep><br></#sep>
    </#list>
  </div>
</#if>
