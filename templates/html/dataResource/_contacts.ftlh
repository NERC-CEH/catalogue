<#if contactPoints?has_content>
  <div id="segment-pointOfContact">
  <h2 id="pointOfContact">Correspondence/contact details</h2>
      <#list contactPoints>
        <#items as poc>
          <div class="contact">
             <@func.displayContact poc true true false />
          </div>
        </#items>
      </#list>
  </div>
</#if>

<#if authors?has_content >
  <div id="segment-authors">
  <h2 id="authors">Author<#if authors?size gt 1>s</#if></h2>

      <#list authors>
      <#assign authorMaxSize = 20 >
      <#assign moreAuthors = authors?size-authorMaxSize >

        <#items as author>
          <#assign rpClass="contact">
          <#if author?counter gt authorMaxSize>
            <#assign rpClass="contact contactCollapse collapse">
          </#if>

          <div class="${rpClass}">
            <@func.displayContact author false false true />
          </div>
        </#items>

      </#list>

      <#if authors?size gt authorMaxSize >
        <a href=".contactCollapse" class="moreAuthors" data-extra-authors="${moreAuthors}" data-bs-toggle="collapse" aria-controls="contactCollapse" area-expanded="false" role="button">
          authors
        </a>
      </#if>

  </div>
</#if>


<#assign otherContactsDisplay = otherContacts >
<#if publishers?has_content>
  <#assign otherContactsDisplay = otherContactsDisplay + publishers >
</#if>
<#if rightsHolders?has_content>
  <#assign otherContactsDisplay = otherContactsDisplay + rightsHolders >
</#if>
<#if custodians?has_content>
  <#assign otherContactsDisplay = otherContactsDisplay + custodians >
</#if>

<#if otherContactsDisplay?has_content >
  <div id="segment-otherContacts">
    <h2><a id="otherContacts"></a>Other contacts</h2>
    <div class="row key-value">
      <#list otherContactsDisplay as otherContact>
        <div class="col-sm-3 key">${otherContact.roleDisplayName?cap_first}</div>
        <div class="col-sm-9 value">
          <div class="contact">
            <@func.displayContact otherContact false true false />
          </div>
        </div>
      </#list>
    </div>
  </div>
</#if>
