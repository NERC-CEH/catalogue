<#list authors>
<div id="document-authors">
<h3 id="authors">Authors</h3>
<#items as author>

  <div class="responsibleParty">
   ${func.displayContact(author, false)}
  <#if author.nameIdentifier?has_content>
    <div class="nameIdentifier">
    <#if author.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
      <#assign nameScheme="ORCID", nameSchemeURL="https://orcid.org/">
      <a href="${author.nameIdentifier}" target="_blank"><img src="/static/img/orcid_16x16.png" alt="ORCID iD icon" title="ORCID iD"> ${author.nameIdentifier?html}</a>
    </#if>
    </div>
  </#if>
  </div>
</#items>
</div>
</#list>
