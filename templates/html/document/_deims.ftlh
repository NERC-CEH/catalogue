<#if deimsSites??>
  <div id="deims-sites">
    <h3>DEIMS Sites</h3>
    <#list deimsSites as site>
      <a href="${site.url}">${site.title}</a><#sep>,
    </#list>
  </div>
</#if>
