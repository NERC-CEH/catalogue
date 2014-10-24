<div id="section-spatial">
	<h3>Spatial</h3>
	<dl>

		<#if spatialRepresentationTypes?has_content>
		<dt>Spatial representation type</dt>
		<#list spatialRepresentationTypes as spatialRepresentationType>
		  <dd>${spatialRepresentationType}<#if spatialRepresentationType_has_next><br></#if></dd>
		</#list>
		</#if>


		<#if spatialReferenceSystem?has_content>
		<dt>Spatial reference system</dt>
		  <p>${spatialReferenceSystem.title}</p>
		</#if>
		
			
	</dl>
</div>
