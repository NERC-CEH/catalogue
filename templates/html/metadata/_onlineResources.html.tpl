<#if onlineResources?has_content>

  <div id="section-onlineResources">
    <h3>Online Resources</h3>
      <#list onlineResources as link>
        <div>
          <p><a href="${link.url?html}" target="_blank">${link.name?html}</a><br>
          <span class="text-muted">${link.description?html}</span><p>
        </div>
      </#list>
  </div>
</#if>
