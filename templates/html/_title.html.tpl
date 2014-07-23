<div class="page-header">
  <div class="row">
    <#if resourceType?has_content>
    <div class="col-md-2 col-md-push-10">
        <h2 id="resource-type" property="dc:type" class="label label-${resourceType.value} pull-right">
          ${resourceType.value}
        </h2>
    </div>
    </#if>
    <#if title?has_content>
    <h2 id="document-title" property="dc:title" class="col-md-10 col-md-pull-2">
	      ${title}
    </h2>
    </#if>
    <#--XXX: Somewhere to expose ID via RDFa, should it be here?-->
    <#if id?has_content>
    <p id="identifier" property="dc:identifier" class="hide" >
      ${id}
    </p>
    </#if>
   </div>
</div>