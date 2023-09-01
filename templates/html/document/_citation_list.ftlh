<#if incomingCitations?has_content>
  <#assign displayCitations = 1>

  <div id="section-citations">
    <h2>Citations <a name="citations"></a></h2>

    <#list incomingCitations as ic>
      <div class="citation">
        <#if ic.description?has_content>
            <span class="citation_description">${ic.description}</span>
        </#if>
        <#if ic.url?has_content>
            <span class="citation_url">
            <a href="${ic.url}" target='_blank' rel="noopener noreferrer">${ic.url}</a></span>
        </#if>
      </div>
    </#list>
      
  </div>
</#if>