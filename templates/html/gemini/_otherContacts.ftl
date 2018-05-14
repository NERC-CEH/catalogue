<#if otherContacts?has_content>
  <div id="document-otherContacts">
  <h3><a id="otherContacts"></a>Other contacts</h3>
  <dl class="dl-horizontal">

  <#if otherContacts?has_content>
    <#list otherContacts as otherContact>
      <dt>${otherContact.roleDisplayName?html}</dt>
      <dd>
        <div class="responsibleParty">      
        <#if otherContact.role == 'pointOfContact'>
          ${func.displayContact(otherContact, true, false)}
        <#else>
          ${func.displayContact(otherContact, false, false)}
        </#if>
        </div>
  	 </dd>
    </#list>
  </#if>
  </dl>
  </div>
</#if>  
