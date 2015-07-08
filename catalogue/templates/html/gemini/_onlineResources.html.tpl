<#if onlineResources?has_content>

  <div id="section-onlineResources">
    <h3>Online Resources</h3>
      <#list onlineResources as link>
        <p>
            <#if link.name?has_content>
                <#assign onlineResourceName=link.name?html>
            <#else>
                <#assign onlineResourceName>Resource ${link_index + 1}</#assign>
            </#if>
          <a href="${link.url?html}" target="_blank">${onlineResourceName}</a><br>
          <span class="text-muted">${link.description?html}</span>
        <p>
      </#list>
  </div>
</#if>
