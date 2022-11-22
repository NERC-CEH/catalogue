<#if licences?has_content>
  <#list licences as licence>
    <#if resourceStatus?? && resourceStatus == "Embargoed">
      <#assign licenceText = licence.value?replace("is made available","will be available")?replace("is available","will be available") >
      <#else> 
      <#assign licenceText = licence.value >
    </#if>

    <p class="licenceText">
      <#if licence.uri?has_content>
        <a rel="license noopener noreferrer" href="${licence.uri}" target="_blank">
      </#if>
      ${licenceText} 
      <#if licence.uri?has_content>
        </a>
      </#if>
    </p>   
  </#list>
</#if>
