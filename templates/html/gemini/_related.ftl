<#assign relatedServices = func.filterRegex(jena.inverseRelationships(uri, "http://vocabs.ceh.ac.uk/eidc#uses"), "associationType", "service")>

<#if relatedServices?? && relatedServices?size gt 0>
  <div class="panel panel-default" id="document-related">
    <div class="panel-heading"><p class="panel-title">
	  <#if resourceType.value == 'dataset'>
		Services associated with this dataset
	  <#elseif resourceType.value == 'service'>
		Datasets associated with this service
	  </#if></p>
	</div>
    <div class="panel-body">
	  <#list relatedServices as service>
		<p><a href="${service.href?html}">${service.title?html}</a></p>
	  </#list>
    </div>
  </div>
</#if>
