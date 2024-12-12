<#if licences?has_content>
  <#list licences as licence>
      <p class="licenceText">

       <#if resourceStatus?? && resourceStatus == "Embargoed">
        <#assign licenceText = licence.value?replace("is made available","will be available")?replace("is available","will be available") >
       <#else> 
        <#assign licenceText = licence.value >
      </#if>
      <#if recordType?? && recordType?has_content>
        <#assign licenceText = licenceText?replace("resource",recordType) >
      </#if>

        <#if licence.uri?has_content><a rel="license" href="${licence.uri}"></#if>
          ${licenceText}
          <#if licenceText?contains("Open Government Licence")>
            <img class="ogl-logo" src='/static/img/ogl_16.png' alt='OGL'>
          </#if>
        <#if licence.uri?has_content></a></#if>
      </p>

  </#list>
</#if>
