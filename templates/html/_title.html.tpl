<div class="page-header">
  <div class="row">
    <div class="col-md-2 col-md-push-10">
      <#if resourceType?has_content>
        <h2 id="resource-type" property="dc:type" class="label label-${resourceType.value} pull-right">
          ${resourceType.value}
        </h2>
      </#if>
    </div>
    <h2 id="document-title" property="dc:title" class="col-md-10 col-md-pull-2">
	    <#if title?has_content>
	      ${title}
	    </#if>
    </h2>
    <#--XXX: Somewhere to expose ID via RDFa, should it be here?-->
    <#if id?has_content>
    <p id="identifier" property="dc:identifier" class="hide" >
      ${id}
    </p>
    </#if>
   </div>
</div>