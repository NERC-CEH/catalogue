<#if downloadOrder.orderUrl?has_content||mapViewable >
<div class="panel panel-default hidden-print" id="document-order">
	<div class="panel-heading"><p class="panel-title">Get the data</p></div>
	<div class="panel-body">
		
		<p><#if downloadOrder.orderUrl?has_content>
		<a href="#" property="dcat:accessURL"><i class="glyphicon glyphicon-save text-info"></i> Order/download</a></p>
		</#if>
		<#if mapViewable>
		<p><i class="glyphicon glyphicon-map-marker text-info"></i> Preview on map</p>
		</#if>
		<#if downloadOrder.supportingDocumentsUrl?has_content>
		<p><i class="glyphicon glyphicon-book text-info"></i> <a href="#" title="Supporting information important for the re-use of this dataset">Supporting documentation</a></p>
		</#if>
		
		
		
	</div>
	
	<div class="panel-body" id="section-formats">
		<#include "_formats.html.tpl">
	</div>
	
	<div class="panel-body" id="section-licence">
		<#include "_accessInfo.html.tpl">
	</div>

</div>
</#if>