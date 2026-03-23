<#if licences?has_content || otherConstraints?has_content>
  <div id="segment-constraints">
    <h2>Licensing and constraints</h2>
    <#include "_status.ftlh">

    <#list licences as licence>
        <p class="licenceLabel">
        <#assign licenceLabel = "Licence terms and conditions apply" >
        <#if licence.value ?? && licence.value?has_content>
          <#assign licenceLabel = licence.value >
        </#if>

        <#if availability?? && availability == "Embargoed">
          <#assign licenceLabel = licenceLabel?replace("is made available","will be available")?replace("is available","will be available") >
        </#if>
        <#if recordType?? && recordType?has_content>
          <#assign licenceLabel = licenceLabel?replace("resource",recordType) >
        </#if>

          <#if licence.uri?has_content><a rel="license" href="${licence.uri}"></#if>
            ${licenceLabel}
            <#if licenceLabel?contains("Open Government Licence")>
              <img class="ogl-logo" src='/static/img/ogl_16.png' alt='OGL'>
            </#if>
          <#if licence.uri?has_content></a></#if>
        </p>
    </#list>

    <#include "_citation.ftlh">
    <#include "_otherUseConstraints.ftlh">
  </div>
</#if>
