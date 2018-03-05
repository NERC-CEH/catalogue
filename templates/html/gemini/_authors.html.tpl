<#if authors?size gt 0 >
  <div id="document-authors">
  <h3 id="authors">Authors</h3>

      <#list authors>
      <#assign authorMaxSize = 30 >
      <#assign moreAuthors = authors?size-authorMaxSize >
        <#items as author>
          <#assign rpClass="responsibleParty">
          <#if author?counter gt authorMaxSize>
            <#assign rpClass="responsibleParty responsiblePartyCollapse collapse">
          </#if>

          <div class="${rpClass}">
            ${func.displayContact(author, false, true)}
          </div>
            
        </#items>
      
      <#if authors?size gt authorMaxSize >
        <p><small>
          <a href=".responsiblePartyCollapse" class="moreAuthors collapsed" data-toggle="collapse" aria-controls="responsiblePartyCollapse" area-expanded="false"><span>${moreAuthors} more authors. </span>Show </a>
        </small></p>
      </#if>
      </#list>

  </div>
</#if>