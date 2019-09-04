<#if pocs?size gt 0 >
  <div id="document-pointOfContact">
  <h3 id="pointOfContact">Correspondence/contact details</h3>
      <#list pocs>
        <#items as poc>
          <div class="responsibleParty">
            ${func.displayContact(poc, true, true, false)}
          </div>
        </#items>
      </#list>
  </div>
</#if>

<#if authors?size gt 0 >
  <div id="document-authors">
  <h3 id="authors">Author<#if authors?size gt 1>s</#if></h3>

      <#list authors>
      <#assign authorMaxSize = 30 >
      <#assign moreAuthors = authors?size-authorMaxSize >
        <#items as author>
          <#assign rpClass="responsibleParty">
          <#if author?counter gt authorMaxSize>
            <#assign rpClass="responsibleParty responsiblePartyCollapse collapse">
          </#if>

          <div class="${rpClass}">
            ${func.displayContact(author, false, false, true)}
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

<#if otherContacts?has_content>
  <div id="document-otherContacts">
  <h3><a id="otherContacts"></a>Other contacts</h3>
  <dl class="dl-horizontal">

  <#if otherContacts?has_content>
    <#list otherContacts as otherContact>
      <dt>${otherContact.roleDisplayName?html}</dt>
      <dd>
        <div class="responsibleParty">      
          ${func.displayContact(otherContact, false, true, false)}
        </div>
  	 </dd>
    </#list>
  </#if>
  </dl>
  </div>
</#if>  
