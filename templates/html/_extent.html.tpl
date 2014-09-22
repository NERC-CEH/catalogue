<div id="section-extent">
<h3><a id="extent"></a>Extent</h3>
	<dl class=" dl-horizontal">
	 
	<#if boundingBoxes?has_content && boundingBoxes??>
	  <dt>Bounding box</dt>
	  <dd>
			<#list boundingBoxes as extent>
			<figure title="Map showing the spatial extent of this data resource"><img property="dc:spatial" src="${extent.googleStaticMapUrl}" alt="Spatial Extent"></figure>
			</#list>
	  </dd>
	</#if>
	<#if temporalExtent?has_content>
	  <dt>Temporal extent</dt>
	  <dd>
		<span id="temporal-extent" property="dc:temporal" typeof="dc:PeriodOfTime">
			<#list temporalExtent as extent>
			  <#if extent_index gt 0>, </#if>
			  <#setting date_format = 'yyyy-MM-dd'>
			  <#if extent.begin?has_content>
				<span property="dc:start" class="extentBegin">${extent.begin?date}</span>
				<#else>...
			  </#if>
			  to
			  <#if extent.end?has_content>
				<span property="dc:end" class="extentEnd">${extent.end?date}</span>
			  <#elseif resourceStatus?has_content && resourceStatus == "onGoing">present
			  <#else>...
			  </#if>
			</#list>
	    </span>
	  </dd>
	</#if>

	</dl>            
</div>

	<div class="alert alert-danger alert-dismissible" role="alert">
	<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	Does extent need to be added to this page (i.e. country codes)?
	</div>
