<#if documentLinks?size gt 0 && documentLinks??>
  <div class="panel panel-default" id="document-related">
    <div class="panel-heading">
	  <#if resourceType == 'dataset'>
		Services associated with this dataset
	  <#elseif resourceType == 'service'>
		Datasets associated with this service
	  </#if>
	</div>
    <div class="panel-body">
	  <#list documentLinks as link>
		<p><a href="${link.href?html}">${link.title?html}</a></p>
	  </#list>
    </div>
  </div>
</#if>