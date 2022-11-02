<#if deimsSites??>
  <div id="deims-sites">
    <h2>DEIMS Sites</h2>
    <#list deimsSites as site>
      <a href="${site.url}">${site.title}</a><#sep><br></#sep>
    </#list>
  </div>
</#if>
