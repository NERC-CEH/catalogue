<#if licences?has_content>
  <#list licences as licence>
    <#if licence.code == 'license'>
      <p class="licenceText">
       <#if resourceStatus?? && resourceStatus == "Embargoed">
        <#assign licenceText = licence.value?replace("is made available","will be available")?replace("is available","will be available") >
       <#else> 
        <#assign licenceText = licence.value >
      </#if>
      <#if resourceStatus?? && resourceStatus == 'Controlled'>
        <span class="fa-stack" title="Access to this data is controlled - a bespoke licence may need to be negotiated">
          <i class="text-muted fas fa-lock-open fa-stack-1x"></i>
          <i class="fas fa-slash fa-stack-1x"></i>
        </span>
      </#if>

        <#if licence.uri?has_content><a rel="license" href="${licence.uri}"></#if>
          ${licenceText?replace("resource",recordType)?html}
          <#if licenceText?contains("Open Government Licence")>
            <img class="ogl-logo" src='/static/img/ogl_16.png' alt='OGL'>
          </#if>
        <#if licence.uri?has_content></a></#if>
      </p>
      <div class="divider"></div>
    </#if>
  </#list>
</#if>
