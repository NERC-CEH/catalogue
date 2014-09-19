<#if downloadOrder.orderUrl?has_content||mapViewable >
<div class="panel panel-default" id="section-order">
	<div class="panel-heading"><p class="panel-title">Get the data</p></div>
	<div class="panel-body">
		
		<#if downloadOrder.orderUrl?has_content>
		<p><a href="#" property="dcat:accessURL"><i class="glyphicon glyphicon-save text-info"></i> Order/download</a></p>
		</#if>
		<#if mapViewable>
		<p><i class="glyphicon glyphicon-map-marker text-info"></i> Preview on map</p>
		</#if>
		<#if downloadOrder.supportingDocumentsUrl?has_content>
		<p><span class="popover-dismiss" data-toggle="popover" data-content="Link to the EIDC Hub landing page (which will have to be redesigned)" data-original-title="" title=""><i class="glyphicon glyphicon-book text-info"></i> <a href="#" title="Supporting information important for the re-use of this dataset, and also link to data citation details">Supporting documentation</a></span></p>
		</#if>
	</div>
	<#if downloadOrder.licenseUrl?has_content>
	<div class="panel-footer">
	<a href="${downloadOrder.licenseUrl}" target="_blank" property="dct:rights"><#if downloadOrder.isOgl()><img src="../../_assets/images/ogl.png" alt="OGL" class="pull-right hidden-xs"> </#if>${licenseText}</a>
	</div>
	</#if>
</div>
</#if>