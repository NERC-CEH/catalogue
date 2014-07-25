
<#if spatialReferenceSystem?? && spatialReferenceSystem.title?? && spatialReferenceSystem.title?has_content>
  <div class="row">
    <div class="col-md-12">
			<div class="spatial-reference-system">
			  <h3>Spatial Reference System</h3>
			  <p>${spatialReferenceSystem.title}</p>
			</div>
    </div>
  </div>
</#if>
