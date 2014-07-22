<div class="page-header">
  <h2>  	
  <div class="row">
    <#if resourceType?has_content>
    <div class="col-md-2 col-md-push-10">			
			<span class="label label-${resourceType.value} pull-right">${resourceType.value}</span>  
    </div>
    </#if>
    <#if title?has_content>
    <div id="document-title" class="col-md-10 col-md-pull-2">
	    ${title}  
    </div>
    </#if>
   </div>
  </h2>
</div>