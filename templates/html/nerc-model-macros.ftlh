
<#macro key key alt="" auto_esc=false>
  <@b.basicRow "key-value">
    <@m.keyContent key>
      <#nested>
    </@m.keyContent>
  </@b.basicRow>
</#macro>

<#macro keyContent key>
  <div class="col-sm-3 key">
    <div class="key-name">
      ${key}
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
<#macro qa qa={"done": "unknown"}>
  <div>
    <#if qa.done?? && qa.done?has_content>
      <span class="key">done?</span> ${qa.done?cap_first}
    </#if>
    <#if qa.modelVersion?? && qa.modelVersion?has_content>
      <span class="key">model version</span> ${qa.modelVersion}
    </#if>
    <#if qa.owner?? && qa.owner?has_content>
      <span class="key">owner</span> ${qa.owner}
    </#if>
    <#if qa.date?? && qa.date?has_content>
      <span class="key">date</span> ${qa.date}
    </#if>
  </div>
  <div>
    <#if qa.note?? && qa.note?has_content>
      <span class="key">note</span> ${qa.note}
    </#if>
  </div>
</#macro>
  
<#--
CEH dataInfo table
-->
<#macro dataInfoTable data>
<table class="table table-condensed">
<thead><tr><th>variable name</th><th>units</th><th>file format</th><th>url</th></tr></thead>
<tbody>
  <#list data as item>
    <tr>
      <td>
        <#if item.variableName?? && item.variableName?has_content>${item.variableName}</#if>
      </td>
      <td>
        <#if item.units?? && item.units?has_content>${item.units}</#if>
      </td>
      <td>
        <#if item.fileFormat?? && item.fileFormat?has_content>${item.fileFormat}</#if>
      </td>
      <td>
        <#if item.url?? && item.url?has_content><@b.bareUrl item.url/></#if>
      </td>
    </tr>
  </#list>
</tbody>
</table>
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