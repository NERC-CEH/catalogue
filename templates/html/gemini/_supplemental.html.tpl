<#if supplemental?has_content>

  <div id="section-supplemental">
    <h3>Supplemental information</h3>
      <#list supplemental as supplement>
        <p>
          <#if supplement.name?has_content>
            <#if supplement.url?has_content>
            <a href="${supplement.url?html}" target="_blank">${supplement.name?html}</a>
            </#if>
          <#else>
            <#if supplement.url?has_content>
            <a href="${supplement.url?html}" target="_blank">${supplement.url}</a>
            </#if>
          </#if>
          <#if supplement.description?has_content>
            <br>${supplement.description?html}
          </#if>
          <#if supplement.url?has_content>
            <br><small><a href="${supplement.url?html}" target="_blank">(${supplement.url})</small></a>
          </#if>
        <p>
      </#list>
  </div>
</#if>
