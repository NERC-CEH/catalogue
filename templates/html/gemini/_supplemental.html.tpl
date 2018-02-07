<#if supplemental?has_content>

  <div id="section-supplemental">
    <h3>Supplemental information</h3>
      <#list supplemental as supplement>
        <div class="supplemental-item">
          <#if supplement.type == "website">
            <div>
            <#if supplement.name?has_content>
              <#if supplement.url?has_content>
                <a href="${supplement.url?html}" target="_blank" rel="noopener" title="${supplement.url}">${supplement.name?html}</a>
              </#if>
            <#else>
              <#if supplement.url?has_content>
                <a href="${supplement.url?html}" target="_blank" rel="noopener">${supplement.url}</a>
              </#if>
            </#if>
            </div>
            <#if supplement.description?has_content>
              <div>${supplement.description?html}</div>
            </#if>
          <#else>
              <#if supplement.description?has_content>
                <div>${supplement.description?html}</div>
              </#if>
              <#if supplement.url?has_content>
                <div><a href="${supplement.url?html}" target="_blank" rel="noopener">${supplement.url}</a></div>
              </#if>
          </#if>
        </div>
      </#list>
  </div>
</#if>
