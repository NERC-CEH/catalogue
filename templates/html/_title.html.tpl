<div class="page-header">
  <div class="row">
    <div class="col-md-2 col-md-push-10">
      <#if resourceType?has_content>
        <h2 id="resource-type" property="dc:type" class="label label-${resourceType.value} pull-right">
          ${resourceType.value}
        </h2>
      </#if>
    </div>
    <h2 property="dc:title" id="document-title" class="col-md-10 col-md-pull-2">
	    <#if title?has_content>
	      ${title}
	    </#if>
      <#--XXX: Somewhere to expose ID via RDFa, should it be here?-->
      <#if id?has_content>
      <span class="hide" id="identifier" property="dc:identifier">
        ${id}
      </span>
      </#if>
    </h2>
   </div>
</div>