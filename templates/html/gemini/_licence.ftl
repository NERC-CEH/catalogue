<#if licences?has_content>
  <#list licences as licence>
    <#if licence.code == 'license'>
      <p class="licenceText">

       <#if resourceStatus?? && resourceStatus == "Embargoed">
        <#assign licenceText = licence.value?replace("is made available","will be available")?replace("is available","will be available") >
       <#else> 
        <#assign licenceText = licence.value >
      </#if>

        <#if licence.uri?has_content>
          <a href="${licence.uri}">${licenceText?replace("resource",recordType)?html}</a>
        <#else>
          ${licenceText?html}
        </#if>
      </p>
      <div class="divider"></div>
    </#if>
  </#list>
</#if>
