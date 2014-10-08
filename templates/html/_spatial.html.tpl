<div id="section-spatial">
	<h3>Spatial</h3>
	<dl class="">
		<dt class="placeholder">Spatial representation type(s)</dt>
		<dd class="placeholder">Grid, Table, Triangulated irregular network</dd>
		<#if spatialReferenceSystem?? && spatialReferenceSystem.title?? && spatialReferenceSystem.title?has_content>
		<dt>Spatial Reference System(s)</dt>
		<dd =>${spatialReferenceSystem.title}</dd><!-- there could be multiple!-->
		</#if>
	</dl>
</div>
