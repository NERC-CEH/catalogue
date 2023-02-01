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

<#macro Url value newWindow=true>
  <#if value?matches("^http(s)?://.*")>
    <#if newWindow==true>
      <a href="${value}" target="_blank" rel="noopener noreferrer">${value}</a>
    <#else>
      <a href="${value}">${value}</a>
    </#if>
  </#if>
</#macro>

<#macro urlAccess value class="" newWindow=true>
  <#if value.url?matches("^http(s)?://.*")>
    <#if newWindow==true>
      <a class="${class}" href="${value.url}" title="${value.url}" target="_blank" rel="noopener noreferrer"><#nested></a>
    <#else>
      <a class="${class}" href="${value.url}" title="${value.url}"><#nested></a>
    </#if>
  </#if>
</#macro>

<#macro parameters paramlist>
  <div class="parameterList">
    <#list paramlist>
      <#items as value>
        <#if value.title?? && value.title?has_content>
          ${value.title}
        <#elseif value.name?? && value.name?has_content>
          ${value.name}
        </#if>
        <sep><br></sep>
      </#items>
    </#list>
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
  <div>
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
  </div>
  </#if>
</#macro>

<!-- contacts -->
<#macro displayContact contact showPostal=false>
    <#if contact.individualName?has_content>
      <div class="individualName">
          ${contact.individualName}
          <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
              <span class="nameIdentifier">
                <a href="${contact.nameIdentifier}" target="_blank" rel="noopener noreferrer" title="View this authors ORCiD record ${contact.nameIdentifier}">
                  <img src="/static/img/orcid_16x16.png" alt="ORCID iD icon">
                </a>
              </span>
          </#if>
      </div>
    </#if>

    <#if contact.organisationName?has_content>
      <div class="organisationName">
          ${contact.organisationName}
          <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^http(|s)://ror.org/[\\d\\w]{8,12}$")>
              <span class="organisationIdentifier">
                <a href="${contact.organisationIdentifier}" target="_blank" rel="noopener noreferrer" title="RoR ${contact.organisationIdentifier}">
                  <img src="/static/img/ror_16x16.png" alt="RoR icon">
                </a>
              </span>
          </#if>
      </div>
    </#if>

    <#if showPostal && contact.address?has_content>
      <div class="postalAddress">
          <#if contact.address.deliveryPoint?has_content>
              ${contact.address.deliveryPoint}<br>
          </#if>
          <#if contact.address.city?has_content>
              ${contact.address.city}<br>
          </#if>
          <#if contact.address.administrativeArea?has_content>
              ${contact.address.administrativeArea}<br>
          </#if>
          <#if contact.address.postalCode?has_content>
              ${contact.address.postalCode?upper_case}<br>
          </#if>
          <#if contact.address.country?has_content>
              ${contact.address.country?upper_case}<br>
          </#if>
      </div>
    </#if>

    <#if contact.phone?? && contact.phone?has_content>
        <div class="contactPhone"><i class="fas fa-phone"></i> ${contact.phone}</div>
    </#if>

    <#if contact.email?has_content>
      <a href="mailto:${contact.email}"><i class="far fa-envelope contactEmail"></i> ${contact.email}</a>
    </#if>

</#macro>
