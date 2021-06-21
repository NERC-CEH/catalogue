<div id="sites">
  <h3>Deims Sites</h3>
  <#-- Site -->
  <#list deimsSites as site>
    <a href="${site.url}">${site.title}</a>
  </#list>
</div>