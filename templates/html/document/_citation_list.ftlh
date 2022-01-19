<#if citations?has_content>
  <div id="section-citations">
    <h2>Citations</h2>

    <#list citations as citation>
      <div class="citation">
        <#if citation.description?has_content>
            <span class="citation_description">${citation.description}</span>
        </#if>
        <#if citation.url?has_content>
            <span class="citation_url"><a href="${citation.url}" target='_blank' rel="noopener noreferrer">${citation.url}</a></span>
        </#if>
      </div>
    </#list>
      
  </div>
</#if>