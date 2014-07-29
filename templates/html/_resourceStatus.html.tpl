<#if resourceStatus?has_content>
  <#if resourceStatus == "historicalArchive">
    <div class="alert alert-warning">
      <b>Warning:</b> This version is not current.
    </div>
  <#else>
	<div class="resource_status">
	  <b>Resource status:</b> ${resourceStatus}
	</div>
  </#if>
</#if>