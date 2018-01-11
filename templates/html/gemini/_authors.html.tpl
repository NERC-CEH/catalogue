<#list authors>
<div id="document-authors">
<h3 id="authors">Authors</h3>
<#items as author>
  <div class="responsibleParty">
   ${func.displayContact(author, false, true)}
  </div>
</#items>
</div>
</#list>
