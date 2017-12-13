<#if contacts?has_content>
  <div id="document-otherContacts">
  <h3><a id="otherContacts"></a>Archive Contacts</h3>
  <dl class="dl-horizontal">

    <#list contacts as contact>
      <dt>${contact.roleDisplayName?html}</dt>
      <dd>
        <div class="responsibleParty">      
          ${func.displayContact(contact, true)}
        </div>
  	 </dd>
    </#list>

  </dl>
  </div>
</#if>  
