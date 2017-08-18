<#if supplemental?has_content>

  <div id="section-supplemental">
    <h3>Supplemental information</h3>
      <#list supplemental as supplement>
        <p>
          <#if supplement.name?has_content>
              ${supplement.name?html}
          </#if>
          <#if supplement.url?has_content>
            <br><a href="${supplement.url?html}" target="_blank">${supplement.url}</a>
          </#if>
          <#if supplement.description?has_content>
            <br><span class="text-muted">${supplement.description?html}</span>
          </#if>
        <p>
      </#list>
  </div>
</#if>
