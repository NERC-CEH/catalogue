<#if licences?has_content && (resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Embargoed")>
  <#list licences as licence>
      <#if licence.code == 'license'>
        <p class="licenceText">
        <#if resourceStatus?? && resourceStatus == "Embargoed">
          <#assign licenceText = licence.value?replace("is made available","will be available")?replace("is available","will be available") >
        <#else> 
          <#assign licenceText = licence.value >
        </#if>


          <#if licence.uri?has_content><a rel="license" href="${licence.uri}"></#if>
            ${licenceText?replace("resource",recordType)}
            <#if licenceText?contains("Open Government Licence")>
              <img class="ogl-logo" src='/static/img/ogl_16.png' alt='OGL'>
            </#if>
          <#if licence.uri?has_content></a></#if>
        </p>
      </#if>
  </#list>

  <p class="licencePreamble">By accessing or using this dataset, you agree to the terms of the relevant licence agreement(s). You will ensure that this dataset is cited in any publication that describes research in which the data have been used.</p>

</#if>
