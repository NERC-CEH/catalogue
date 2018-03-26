<#if otherContacts?has_content || distributorContacts??>
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
    
  <#if distributorContacts?has_content>
    <#list distributorContacts as distributorContact>
      <dt>${distributorContact.roleDisplayName?html}</dt>
      <dd>
        ${func.displayContact(distributorContact, true, false)}
      </dd>
    </#list>
  </#if>
  </dl>
  </div>
</#if>  
