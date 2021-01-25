<#macro key key definition="" classes="" auto_esc=false>
  <div class="${classes}">
    <div><strong title="${definition}">${key}</strong></div>
    <div><#nested></div>
  </div>
</#macro>

<#macro keyContent key definition="">
  <div class="col-sm-3 key">
    <div class="key-name">
      <#if definition?has_content>
        <span title="${definition}">${key} <i class="moreinfo fas fa-info" title="${definition}"></i></span>
      <#else>
        ${key}
      </#if>
    </div>
  </div>
  <div class="col-sm-9 value">
    <#nested>
  </div>
</#macro>l

<#macro Url value newWindow=false name="" >
  <#if name?has_content>
    <#local linkname = name>
  <#else>
    <#local linkname = value>
  </#if>

  <#if value?matches("^http(s)?://.*")>
    <#if newWindow==true>
      <a href="${value}" target="_blank" rel="noopener noreferrer">${linkname}</a>
    <#else>
      <a href="${value}">${linkname}</a>
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
  <dl class="dl-horizontal dl-horizontal--compact">
    <dt>Done?</dt><dd>
    <#if qa.done?? && qa.done?has_content>
      <#if qa.done?matches("yes")>
        <i class="fas fa-check text-success"></i>
      <#elseif qa.done?matches("no")>
        <i class="fas fa-times text-danger"></i>
      <#else>
        <i class="fas fa-question text-info"></i>
      </#if>
      ${qa.done?cap_first}
    <#else>
      <i class="fas fa-question text-info"></i> Not specified
    </#if></dd>
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
      <dt>Notes</dt><dd>${qa.note?replace("\n", "<br>")}</dd>
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