<#if documentLinks?size gt 0 && documentLinks??>
  <div class="panel panel-default" id="document-related">
    <div class="panel-heading"><p class="panel-title">
	  <#if resourceType.value == 'dataset'>
		Services associated with this dataset
	  <#elseif resourceType.value == 'service'>
		Datasets associated with this service
	  </#if></p>
	</div>
    <div class="panel-body">
	  <#list documentLinks as link>
		<p><a href="${link.href?html}">${link.title?html}</a></p>
	  </#list>
    </div>
  </div>
</#if>