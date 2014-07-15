<div class="page-header">
  <h2>  	
  <div class="row">
    <div class="col-md-2 col-md-push-10">
			<#if resourceType?has_content>
			  <span class="label label-${resourceType.value} pull-right">${resourceType.value}</span>
			</#if>  
    </div>
    <div id="document-title" class="col-md-10 col-md-pull-2">
	    <#if title?has_content>
	      ${title}
	    </#if>   
    </div>
   </div>
  </h2>
</div>