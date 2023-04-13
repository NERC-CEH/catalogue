<#if onlineResources?? >
  <#assign mapservices = func.filterRegex(onlineResources, "url", "https://catalogue.ceh.ac.uk/maps/")>
</#if>
<div  id="document-distribution">
    <h2>Use this service</h2>
    <div class="distribution-service">
      <#if mapservices??>
      <#list mapservices as wms>
        <p><i class="fa-solid fa-link"></i> URL: <span class="wmsurl">${wms.url}</span></p>
      </#list>
      </#if>
      <#include "_licence.ftlh">
      <#include "_otherUseConstraints.ftlh">
  </div>
</div>