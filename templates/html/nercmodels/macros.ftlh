<#macro key key alt="" auto_esc=false>
  <div class="key-value">
    <div class="key">
      ${key}
    </div>
    <div class="value">
      <#nested>
    </div>
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