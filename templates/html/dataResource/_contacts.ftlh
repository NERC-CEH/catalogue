<#if pointsOfContact?size gt 0 >
  <div id="segment-pointOfContact">
  <h2 id="pointOfContact">Correspondence/contact details</h2>
      <#list pointsOfContact>
        <#items as poc>
          <div class="responsibleParty">
             <@func.displayContact poc true true false />
          </div>
        </#items>
      </#list>
  </div>
</#if>

<#if authors?size gt 0 >
  <div id="segment-authors">
  <h2 id="authors">Author<#if authors?size gt 1>s</#if></h2>

      <#list authors>
      <#assign authorMaxSize = 20 >
      <#assign moreAuthors = authors?size-authorMaxSize >

        <#items as author>
          <#assign rpClass="responsibleParty">
          <#if author?counter gt authorMaxSize>
            <#assign rpClass="responsibleParty responsiblePartyCollapse collapse">
          </#if>

          <div class="${rpClass}">
            <@func.displayContact author false false true />
          </div>
        </#items>

      <#if authors?size gt authorMaxSize >
        <p class="responsiblePartyCollapse trigger">
          <small>
            <a href=".responsiblePartyCollapse" class="moreAuthors" data-bs-toggle="collapse"
            aria-controls="responsiblePartyCollapse" area-expanded="false" role="button">
              <span>${moreAuthors} more authors. </span>Show
            </a>
          </small>
        </p>
      </#if>
      </#list>

  </div>
</#if>

<#if otherContacts?has_content >
  <div id="segment-otherContacts">
    <h2><a id="otherContacts"></a>Other contacts</h2>
    <div class="row key-value">
      <#if otherContacts?has_content>
        <#list otherContacts as otherContact>
          <div class="col-sm-3 key">${otherContact.roleDisplayName?cap_first}</div>
          <div class="col-sm-9 value">
            <div class="responsibleParty">
              <@func.displayContact otherContact false true false />
            </div>
        </div>
        </#list>
      </#if>
    </div>
  </div>
</#if>
