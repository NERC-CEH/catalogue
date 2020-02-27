
<#macro key key definition="" auto_esc=false>
  <@b.basicRow "key-value"> 
    <@keyContent key definition>
      <#nested>
    </@keyContent>
  </@b.basicRow>
</#macro>

<#macro keyContent key definition="">
  <div class="col-sm-3 key">
    <div class="key-name">
      ${key} 
      <#if definition?has_content><span class="moreinfo" title="${definition}"><i class="fas fa-info-circle"> </i></span></#if>
    </div>
  </div>
  <div class="col-sm-9 value">
    <#nested>
  </div>
</#macro>

<#macro Url value newWindow=false>
  <#if value?matches("^http(s)?://.*")>
    <#if newWindow==true>
      <a href="${value}" target="_blank" rel="noopener noreferrer">${value}</a>
    <#else>
      <a href="${value}">${value}</a>
    </#if>
  </#if>
</#macro>

<#macro commaList values>
  <#list values>
    <#items as value>
      ${value}<sep>, </sep>
    </#items>
  </#list>
</#macro>

<#--
CEH model QA
-->
<#macro qa qa={"done": "Not specified"}>
  <dl class="dl-qa">
    <#if qa.done?? && qa.done?has_content>
      <dt>Done?</dt><dd>${qa.done?cap_first}</dd>
    </#if>
    <#if qa.modelVersion?? && qa.modelVersion?has_content>
      <dt>Model version</dt><dd>${qa.modelVersion}</dd>
    </#if>
    <#if qa.owner?? && qa.owner?has_content>
      <dt>Owner</dt><dd>${qa.owner}</dd>
    </#if>
    <#if qa.date?? && qa.date?has_content>
      <dt>Date</dt><dd>${qa.date?date?string['d MMM yyyy']}</dd>
    </#if>
    <#if qa.note?? && qa.note?has_content>
      <dt>Notes</dt><dd>${qa.note}</dd>
    </#if>
  </dl>
</#macro>


<!-- additional metadata -->
<#macro additionalMetadata>
  <#if metadataDate?? && metadataDate?has_content>
  <section>
    <h2>Additional metadata</h2>
    <#if keywords?? && keywords?has_content>
    <@key "Keywords">
      <#list keywords>
        <#items as keyword>
          <#if keyword.uri?? && keyword.uri?has_content>
            <a href="${keyword.uri}">
              <#if keyword.value?? && keyword.value?has_content>
                ${keyword.value}
              <#else>
                  ${keyword.uri}
              </#if>
            </a>
          <#elseif keyword.value?? && keyword.value?has_content>
            ${keyword.value}
          <#else>
            <span class="text-muted">empty</span>
          </#if>
          <#sep>, </#sep>
        </#items>
        </#list>
    </@key>
    </#if>

    <@key "Last updated">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@key>
  </section>
  </#if>
</#macro>