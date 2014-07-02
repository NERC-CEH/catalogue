<div class="page-header">
  <h2>
  	<#if resourceType?has_content>
  	  <span class="label label-primary">${resourceType.value}</span>
  	</#if>
    <#if title?has_content>
      ${title}
    </#if>
  </h2>
</div>