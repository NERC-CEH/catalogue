<#if useConstraints?has_content>
  <#list useConstraints as licence>
    <#if licence.code == 'license'>
      <p class="licenceText">

       <#if resourceStatus == "Embargoed">
        <#assign licenceText = licence.value?replace("is made available","will be available") >
       <#else> 
        <#assign licenceText = licence.value >
      </#if>

        <#if licence.uri?has_content>
          <a href="${licence.uri}">${licenceText?html}</a>
        <#else>
          ${licenceText?html}
        </#if>
      </p>
    </#if>
  </#list>
</#if>
