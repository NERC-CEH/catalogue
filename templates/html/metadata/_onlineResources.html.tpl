<#if onlineResources?has_content>

  <div id="section-onlineResources">
    <h3>Online Resources</h3>
      <#list onlineResources as link>
        <div>
          <p><a href="${link.url}" target="_blank">${link.name}</a><br>
          <span class="text-muted">${link.description}</span><p>
        </div>
      </#list>
  </div>
</#if>
