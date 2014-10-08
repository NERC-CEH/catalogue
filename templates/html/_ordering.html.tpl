<#if downloadOrder.orderUrl?has_content||mapViewable >
<div class="panel panel-default hidden-print" id="document-order">
	<div class="panel-heading"><p class="panel-title">Get the data</p></div>
	<div class="panel-body">
		
		<#if downloadOrder.orderUrl?has_content>
		<p><a href="#" property="dcat:accessURL"><i class="glyphicon glyphicon-save text-info"></i> Order/download</a></p>
		</#if>
		<#if mapViewable>
		<p><i class="glyphicon glyphicon-map-marker text-info"></i> Preview on map</p>
		</#if>
		<#if downloadOrder.supportingDocumentsUrl?has_content>
		<p><i class="glyphicon glyphicon-book text-info"></i> <a href="#" title="Supporting information important for the re-use of this dataset">Supporting documentation</a></p>
		</#if>
	</div>
	<div class="panel-footer">
		<#if downloadOrder.isOgl()><img src="../../_assets/images/ogl.png" alt="OGL" class="pull-right hidden-xs"> </#if>	
		<#if downloadOrder.licenseUrl?has_content>
			<a href="${downloadOrder.licenseUrl}" target="_blank" property="dct:rights">${downloadOrder.licenseUrl}</a>
		<#else>
			<span class="placeholder">Licence text</span>
		</#if>
	</div>
	
</div>
</#if>