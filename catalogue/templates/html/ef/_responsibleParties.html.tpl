<#if responsibleParties?has_content>
  <div id="document-responsibleParties">
    <h3><a id="responsibleParties"></a>Responsible Parties</h3>
    <dl class="dl-horizontal">

    <#if responsibleParties?has_content>
      <#list responsibleParties as rp>
        <dt>${rp.role.title!rp.role.value?html}</dt>
        <dd>  
          <#if rp.individualName?has_content>
            <span>${rp.individualName?html}</span><br>
          </#if>
          <#if rp.organisationName?has_content>
            <span>${rp.organisationName?html}</span><br>
          </#if>
                  
          <#if rp.postalAddress?has_content>
            <address class="hidden-xs">
              <#if rp.postalAddress.street?has_content>${rp.postalAddress.street?html}<br></#if>
              <#list rp.postalAddress.postalArea as area >
                ${area}<br>
              </#list>
              <#if rp.postalAddress.administrativeArea?has_content>${rp.postalAddress.administrativeArea?html}<br></#if>
              <#if rp.postalAddress.postcode?has_content>${rp.postalAddress.postcode?html}<br></#if>
              <#if rp.postalAddress.country?has_content>${rp.postalAddress.country?html}</#if>
            </address>
          </#if>

          <#if rp.email?has_content>
            <address><a href="mailto:${rp.email}">${rp.email}</a></address>
          </#if>

          <#if (rp.onlineResource.href)?has_content>
            <address><a href="${rp.onlineResource.href}" target="_blank">${rp.onlineResource.href}</a></address>
          </#if>
        </dd>
      </#list>
    </#if>
    </dl>
  </div>
</#if>  
